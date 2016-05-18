/*
 * Copyright (C) 2016 An Honest Effort LLC.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.anhonesteffort.btc.event;

import com.lmax.disruptor.EventHandler;
import org.anhonesteffort.btc.book.HeuristicLimitOrderBook;
import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.book.TakeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class OrderBookBuilder implements EventHandler<OrderEvent> {

  private static final Logger log = LoggerFactory.getLogger(OrderBookBuilder.class);

  private final OrderFactory factory = new OrderFactory();
  private final HeuristicLimitOrderBook book;
  private boolean rebuilding = false;

  public OrderBookBuilder(HeuristicLimitOrderBook book) {
    this.book = book;
  }

  @Override
  public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) throws OrderEventException {
    switch (event.getType()) {
      case REBUILD_START:
        book.clear();
        rebuilding = true;
        log.info("rebuilding order book");
        break;

      case REBUILD_END:
        rebuilding = false;
        log.info("order book rebuild complete");
        break;

      case LIMIT_OPEN:
        Order      order  = factory.createLimitOrder(event);
        TakeResult result = book.add(order);
        if (result.getTakeSize() <= 0) {
          if (!rebuilding) { log.info("opened new limit order " + order.getOrderId()); }
        } else {
          throw new OrderEventException("opened limit order took from the book");
        }
        break;

      case LIMIT_DONE:
        Optional<Order> removed = book.remove(event.getSide(), event.getPrice(), event.getOrderId());
        if (removed.isPresent() && !rebuilding) {
          log.info("removed limit order " + removed.get().getOrderId());
        }
        break;

      case LIMIT_CHANGE:
        double          reduce  = event.getOldSize() - event.getNewSize();
        Optional<Order> changed = book.reduce(event.getSide(), event.getPrice(), event.getOrderId(), reduce);
        if (changed.isPresent()) {
          log.info("!!! changed limit order " + event.getOrderId() + " by " + reduce + " !!!");
        } else {
          throw new OrderEventException("changed limit order not found in the book");
        }
        break;
    }
  }

}
