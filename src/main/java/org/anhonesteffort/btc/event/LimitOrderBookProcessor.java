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

import org.anhonesteffort.btc.book.LimitOrderBook;
import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.book.OrderPool;
import org.anhonesteffort.btc.book.TakeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class LimitOrderBookProcessor extends OrderBookProcessor {

  private static final Logger log = LoggerFactory.getLogger(LimitOrderBookProcessor.class);

  public LimitOrderBookProcessor(LimitOrderBook book, OrderPool pool) {
    super(book, pool);
  }

  private Order takePooledLimitOrder(OrderEvent event) throws OrderEventException {
    if (event.getPrice() > 0l && event.getSize() > 0l) {
      return pool.take(event.getOrderId(), event.getSide(), event.getPrice(), event.getSize());
    } else {
      throw new OrderEventException("limit order rx/open event has invalid price or size");
    }
  }

  private Order takePooledLimitOrderChange(OrderEvent change) throws OrderEventException {
    if (change.getPrice() > 0l && change.getNewSize() >= 0l) {
      return pool.take(change.getOrderId(), change.getSide(), change.getPrice(), change.getNewSize());
    } else {
      throw new OrderEventException("limit order change event has invalid price or new size");
    }
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
        TakeResult result    = state.getOrderBook().add(limitOpen);
        if (result.getTakeSize() > 0l) {
          throw new OrderEventException("opened limit order took " + result.getTakeSize() + " from the book");
        } else if (!isRebuilding()) {
          onLimitOrderOpened(limitOpen);
        }
        break;

      case LIMIT_CHANGE:
        if (event.getNewSize() < 0l || event.getNewSize() >= event.getOldSize()) {
          throw new OrderEventException("limit order size can only decrease");
        }

        long            reducedBy   = event.getOldSize() - event.getNewSize();
        Optional<Order> limitChange = state.getOrderBook().reduce(event.getSide(), event.getPrice(), event.getOrderId(), reducedBy);
        if (limitChange.isPresent()) {
          onOpenLimitOrderReduced(limitChange.get(), reducedBy);
          if (limitChange.get().getSizeRemaining() <= 0l) {
            returnPooledOrder(limitChange.get());
          }
        } else {
          Order rxLimitChange = takePooledLimitOrderChange(event);
          onReceivedLimitOrderReduced(rxLimitChange, reducedBy);
          returnPooledOrder(rxLimitChange);
        }
        break;

      case LIMIT_DONE:
        Optional<Order> limitDone = state.getOrderBook().remove(event.getSide(), event.getPrice(), event.getOrderId());

        if (event.getSize() <= 0l) {
          if (limitDone.isPresent() && limitDone.get().getSizeRemaining() > 1l) {
            throw new OrderEventException("order for filled order event was still open on the book with " + limitDone.get().getSizeRemaining());
          } else if (limitDone.isPresent()) {
            returnPooledOrder(limitDone.get());
          }
        } else {
          if (!limitDone.isPresent()) {
            throw new OrderEventException("order for cancel order event not found on the book");
          } else if (Math.abs(event.getSize() - limitDone.get().getSizeRemaining()) > 1l) {
            throw new OrderEventException(
                "order for cancel order event disagrees about size remaining " +
                    event.getSize() + " vs " + limitDone.get().getSizeRemaining()
            );
          } else {
            onLimitOrderCanceled(limitDone.get());
            returnPooledOrder(limitDone.get());
          }
        }
        break;
    }
  }

  protected void onLimitOrderReceived(Order order) {
    log.debug("received new limit order " + order.getOrderId());
  }

  protected void onReceivedLimitOrderReduced(Order order, long reducedBy) {
    log.warn("!!! changed received limit order " + order.getOrderId() + " by " + reducedBy + " !!!");
  }

  protected void onLimitOrderOpened(Order order) {
    log.debug("opened new limit order " + order.getOrderId());
  }

  protected void onOpenLimitOrderReduced(Order order, long reducedBy) {
    log.warn("!!! changed open limit order " + order.getOrderId() + " by " + reducedBy + " !!!");
  }

  protected void onLimitOrderCanceled(Order order) {
    log.debug("canceled limit order " + order.getOrderId());
  }

}
