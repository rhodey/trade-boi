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

import org.anhonesteffort.btc.book.HeuristicLimitOrderBook;
import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.book.OrderPool;
import org.anhonesteffort.btc.book.TakeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class LimitOrderBookBuilder extends OrderBookBuilder {

  private static final Logger log = LoggerFactory.getLogger(LimitOrderBookBuilder.class);

  public LimitOrderBookBuilder(HeuristicLimitOrderBook book, OrderPool pool) {
    super(book, pool);
  }

  protected void onLimitOrderOpened(Order order) {
    log.info("opened new limit order " + order.getOrderId());
  }

  protected void onLimitOrderRemoved(Order order) {
    log.info("removed limit order " + order.getOrderId());
  }

  protected void onLimitOrderReduced(Order order, double size) {
    log.info("!!! changed limit order " + order.getOrderId() + " by " + size + " !!!");
  }

  @Override
  protected void onEvent(OrderEvent event) throws OrderEventException {
    switch (event.getType()) {
      case LIMIT_OPEN:
        Order      order  = takePooledLimitOrder(event);
        TakeResult result = book.add(order);
        if (result.getTakeSize() <= 0) {
          if (!isRebuilding()) { onLimitOrderOpened(order); }
        } else {
          throw new OrderEventException("opened limit order took from the book");
        }
        break;

      case LIMIT_DONE:
        Optional<Order> removed = book.remove(event.getSide(), event.getPrice(), event.getOrderId());
        if (removed.isPresent()) {
          onLimitOrderRemoved(removed.get());
          returnPooledOrder(removed.get());
        }
        break;

      case LIMIT_CHANGE:
        double          reduce  = event.getOldSize() - event.getNewSize();
        Optional<Order> changed = book.reduce(event.getSide(), event.getPrice(), event.getOrderId(), reduce);
        if (changed.isPresent()) {
          onLimitOrderReduced(changed.get(), reduce);
          if (changed.get().getSizeRemaining() <= 0) {
            returnPooledOrder(changed.get());
          }
        } else {
          throw new OrderEventException("changed limit order not found in the book");
        }
        break;
    }
  }

}
