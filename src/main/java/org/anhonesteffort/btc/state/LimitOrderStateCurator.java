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

package org.anhonesteffort.btc.state;

import org.anhonesteffort.btc.book.LimitOrderBook;
import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.book.OrderPool;
import org.anhonesteffort.btc.book.TakeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class LimitOrderStateCurator extends StateCurator {

  private static final Logger log = LoggerFactory.getLogger(LimitOrderStateCurator.class);

  public LimitOrderStateCurator(LimitOrderBook book, OrderPool pool) {
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
        if (state.getRxLimitOrders().put(limitRx.getOrderId(), limitRx) != null) {
          throw new OrderEventException("limit order " + limitRx.getOrderId() + " already in the rx state map");
        } else {
          onLimitOrderReceived(limitRx);
        }
        break;

      case LIMIT_OPEN:
        Optional<Order> oldLimit = Optional.ofNullable(state.getRxLimitOrders().remove(event.getOrderId()));
        if (!oldLimit.isPresent()) {
          throw new OrderEventException("limit order " + event.getOrderId() + " was never in the rx state map");
        } else {
          returnPooledOrder(oldLimit.get());
        }

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
          Optional<Order> oldLimitChange = Optional.ofNullable(
              state.getRxLimitOrders().remove(event.getOrderId())
          );

          if (!oldLimitChange.isPresent()) {
            throw new OrderEventException("limit order for change event not found in the rx state map");
          } else {
            Order newLimitChange = takePooledLimitOrderChange(event);
            state.getRxLimitOrders().put(newLimitChange.getOrderId(), newLimitChange);
            onReceivedLimitOrderReduced(newLimitChange, reducedBy);
            returnPooledOrder(oldLimitChange.get());
          }
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