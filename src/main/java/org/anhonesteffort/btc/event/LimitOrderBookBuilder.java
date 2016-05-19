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

  protected void onLimitOrderReceived(Order order) {
    log.info("received new limit order " + order.getOrderId());
  }

  protected void onLimitOrderOpened(Order order) {
    log.info("opened new limit order " + order.getOrderId());
  }

  protected void onLimitOrderReduced(Order order, double size) {
    log.info("!!! changed limit order " + order.getOrderId() + " by " + size + " !!!");
  }

  protected void onLimitOrderCanceled(Order order) {
    log.info("canceled limit order " + order.getOrderId());
  }

  protected void onLimitOrderFilled(Order order) {
    log.info("filled limit order " + order.getOrderId());
  }

  @Override
  protected void onEvent(OrderEvent event) throws OrderEventException {
    switch (event.getType()) {
      case LIMIT_RX:
        Order limitRx = takePooledLimitOrder(event);
        onLimitOrderReceived(limitRx);
        returnPooledOrder(limitRx);
        break;

      case LIMIT_OPEN:
        Order      limitOpen = takePooledLimitOrder(event);
        TakeResult result    = book.add(limitOpen);
        if (result.getTakeSize() <= 0) {
          if (!isRebuilding()) { onLimitOrderOpened(limitOpen); }
        } else {
          throw new OrderEventException("opened limit order took from the book");
        }
        break;

      case LIMIT_CHANGE:
        double          reduce      = event.getOldSize() - event.getNewSize();
        Optional<Order> limitChange = book.reduce(event.getSide(), event.getPrice(), event.getOrderId(), reduce);
        if (limitChange.isPresent()) {
          onLimitOrderReduced(limitChange.get(), reduce);
          if (limitChange.get().getSizeRemaining() <= 0) {
            returnPooledOrder(limitChange.get());
          }
        } else {
          throw new OrderEventException("changed limit order not found in the book");
        }
        break;

      case LIMIT_DONE:
        Optional<Order> limitDone = book.remove(event.getSide(), event.getPrice(), event.getOrderId());
        if (limitDone.isPresent()) {
          if (limitDone.get().getSize() > 0) {
            onLimitOrderCanceled(limitDone.get());
          } else {
            onLimitOrderFilled(limitDone.get());
          }
          returnPooledOrder(limitDone.get());
        }
        break;
    }
  }

}
