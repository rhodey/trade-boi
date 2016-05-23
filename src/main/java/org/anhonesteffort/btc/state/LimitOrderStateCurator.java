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
import org.anhonesteffort.btc.compute.Computation;

import java.util.Optional;
import java.util.Set;

public class LimitOrderStateCurator extends StateCurator {

  public LimitOrderStateCurator(LimitOrderBook book, OrderPool pool, Set<Computation> computations) {
    super(book, pool, computations);
  }

  private Order takePooledLimitOrder(OrderEvent event) throws OrderEventException {
    if (event.getPrice() > 0l && event.getSize() > 0l) {
      return pool.take(event.getOrderId(), event.getSide(), event.getPrice(), event.getSize());
    } else {
      throw new OrderEventException("limit order rx/open event has invalid price or size");
    }
  }

  private Order takePooledRxLimitOrderChange(Order rxLimit, OrderEvent change) throws OrderEventException {
    if (change.getPrice() != rxLimit.getPrice()) {
      throw new OrderEventException("limit order change event disagrees with rx limit order on price");
    } else if (change.getNewSize() >= rxLimit.getSize()) {
      throw new OrderEventException("limit order change event new size is >= rx limit order size");
    } else {
      return pool.take(change.getOrderId(), change.getSide(), change.getPrice(), change.getNewSize());
    }
  }

  @Override
  protected void onEvent(OrderEvent event) throws OrderEventException {
    switch (event.getType()) {
      case LIMIT_RX:
        Order rxLimit = takePooledLimitOrder(event);
        if (state.getRxLimitOrders().put(rxLimit.getOrderId(), rxLimit) != null) {
          throw new OrderEventException("limit order " + rxLimit.getOrderId() + " already in the limit rx state map");
        }
        break;

      case LIMIT_OPEN:
        Optional<Order> oldLimit = Optional.ofNullable(state.getRxLimitOrders().remove(event.getOrderId()));
        if (!oldLimit.isPresent() && !isRebuilding()) {
          throw new OrderEventException("limit order " + event.getOrderId() + " was never in the limit rx state map");
        } else if (oldLimit.isPresent() && Math.abs(oldLimit.get().getSizeRemaining() - event.getSize()) > 1l) {
          throw new OrderEventException(
              "rx limit order for limit open event disagrees about open size, " +
                  "event wants " + event.getSize() + ", state has " + oldLimit.get().getSizeRemaining()
          );
        } else if (oldLimit.isPresent()) {
          returnPooledOrder(oldLimit.get());
        }

        Order      openLimit = takePooledLimitOrder(event);
        TakeResult result    = state.getOrderBook().add(openLimit);
        if (result.getTakeSize() > 0l) {
          throw new OrderEventException("opened limit order took " + result.getTakeSize() + " from the book");
        }
        break;

      case LIMIT_CHANGE:
        if (event.getNewSize() < 0l || event.getOldSize() < 0l) {
          throw new OrderEventException("limit order change event was parsed incorrectly");
        } else if (event.getNewSize() >= event.getOldSize()) {
          throw new OrderEventException("limit order size can only decrease");
        }

        long            reducedBy      = event.getOldSize() - event.getNewSize();
        Optional<Order> changedRxLimit = Optional.ofNullable(state.getRxLimitOrders().remove(event.getOrderId()));
        Optional<Order> changedLimit   = state.getOrderBook().reduce(event.getSide(), event.getPrice(), event.getOrderId(), reducedBy);

        if (changedRxLimit.isPresent() && changedLimit.isPresent()) {
          throw new OrderEventException("order for limit change event was in the limit rx state map and open on the book");
        } else if (changedRxLimit.isPresent()) {
          Order newRxLimit = takePooledRxLimitOrderChange(changedRxLimit.get(), event);
          state.getRxLimitOrders().put(newRxLimit.getOrderId(), newRxLimit);
          returnPooledOrder(changedRxLimit.get());
        } else if (!changedLimit.isPresent()) {
          throw new OrderEventException("order for limit change event not found on the book");
        } else if (changedLimit.get().getSizeRemaining() <= 0l) {
          returnPooledOrder(changedLimit.get());
        }
        break;

      case LIMIT_DONE:
        Optional<Order> doneRxLimit = Optional.ofNullable(state.getRxLimitOrders().remove(event.getOrderId()));
        Optional<Order> doneLimit   = state.getOrderBook().remove(event.getSide(), event.getPrice(), event.getOrderId());

        if (doneRxLimit.isPresent() && doneLimit.isPresent()) {
          throw new OrderEventException("order for limit done event was in the limit rx state map and open on the book");
        } else if (doneRxLimit.isPresent() && Math.abs(doneRxLimit.get().getSizeRemaining() - event.getSize()) > 1l) {
          throw new OrderEventException(
              "rx limit order for limit done event disagrees about size remaining, " +
                  "event wants " + event.getSize() + ", state has " + doneRxLimit.get().getSizeRemaining()
          );
        } else if (doneRxLimit.isPresent()) {
          returnPooledOrder(doneRxLimit.get());
          return;
        }

        if (event.getSize() <= 0l) {
          if (doneLimit.isPresent() && doneLimit.get().getSizeRemaining() > 1l) {
            throw new OrderEventException("order for filled order event was still open on the book with " + doneLimit.get().getSizeRemaining());
          } else if (doneLimit.isPresent()) {
            returnPooledOrder(doneLimit.get());
          }
        } else {
          if (!doneLimit.isPresent()) {
            throw new OrderEventException("order for cancel order event not found on the book");
          } else if (Math.abs(event.getSize() - doneLimit.get().getSizeRemaining()) > 1l) {
            throw new OrderEventException(
                "order for cancel order event disagrees about size remaining " +
                    event.getSize() + " vs " + doneLimit.get().getSizeRemaining()
            );
          } else {
            returnPooledOrder(doneLimit.get());
          }
        }
        break;
    }
  }

}
