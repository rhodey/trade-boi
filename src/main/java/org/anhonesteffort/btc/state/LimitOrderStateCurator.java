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

  private void returnRxLimitForOpen(OrderEvent open) throws OrderEventException {
    Optional<Order> rxLimit = Optional.ofNullable(state.getRxLimitOrders().remove(open.getOrderId()));
    if (!rxLimit.isPresent() && !isRebuilding()) {
      throw new OrderEventException("limit order " + open.getOrderId() + " was never in the limit rx state map");
    } else if (rxLimit.isPresent() && Math.abs(rxLimit.get().getSizeRemaining() - open.getSize()) > 1l) {
      throw new OrderEventException(
          "rx limit order for limit open event disagrees about open size, " +
              "event wants " + open.getSize() + ", rx has " + rxLimit.get().getSizeRemaining()
      );
    } else if (rxLimit.isPresent()) {
      returnPooledOrder(rxLimit.get());
    }
  }

  private long getSizeReducedForChange(OrderEvent change) throws OrderEventException {
    if (change.getNewSize() >= change.getOldSize()) {
      throw new OrderEventException("limit order size can only decrease");
    } else {
      return change.getOldSize() - change.getNewSize();
    }
  }

  private Order takePooledRxLimitChange(Order rxLimit, OrderEvent change) throws OrderEventException {
    if (change.getNewSize() >= rxLimit.getSize()) {
      throw new OrderEventException("limit order change event new size is >= rx limit order size");
    } else {
      return pool.take(change.getOrderId(), change.getSide(), change.getPrice(), change.getNewSize());
    }
  }

  private void checkAndReturnDoneRxLimit(OrderEvent done, Order rxLimit) throws OrderEventException {
    if (Math.abs(rxLimit.getSizeRemaining() - done.getSize()) > 1l) {
      throw new OrderEventException(
          "rx limit order for limit done event disagrees about size remaining, " +
              "event wants " + done.getSize() + ", rx has " + rxLimit.getSizeRemaining()
      );
    } else {
      returnPooledOrder(rxLimit);
    }
  }

  private void checkAndReturnFilledLimit(Order fillLimit) throws OrderEventException {
    if (fillLimit.getSizeRemaining() > 1l) {
      throw new OrderEventException("order for filled order event was still open on the book with " + fillLimit.getSizeRemaining());
    } else {
      returnPooledOrder(fillLimit);
    }
  }

  private void checkAndReturnCanceledLimit(OrderEvent done, Order cancelLimit) throws OrderEventException {
    if (Math.abs(done.getSize() - cancelLimit.getSizeRemaining()) > 1l) {
      throw new OrderEventException(
          "order for cancel order event disagrees about size remaining, " +
              "event wants " + done.getSize() + ", order has " + cancelLimit.getSizeRemaining()
      );
    } else {
      returnPooledOrder(cancelLimit);
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
        returnRxLimitForOpen(event);
        Order      openLimit = takePooledLimitOrder(event);
        TakeResult result    = state.getOrderBook().add(openLimit);
        if (result.getTakeSize() > 0l) {
          throw new OrderEventException("opened limit order took " + result.getTakeSize() + " from the book");
        }
        break;

      case LIMIT_CHANGE:
        long            reducedBy      = getSizeReducedForChange(event);
        Optional<Order> changedRxLimit = Optional.ofNullable(state.getRxLimitOrders().remove(event.getOrderId()));
        Optional<Order> changedLimit   = state.getOrderBook().reduce(event.getSide(), event.getPrice(), event.getOrderId(), reducedBy);

        if (changedRxLimit.isPresent() && changedLimit.isPresent()) {
          throw new OrderEventException("order for limit change event was in the limit rx state map and open on the book");
        } else if (changedRxLimit.isPresent()) {
          Order newRxLimit = takePooledRxLimitChange(changedRxLimit.get(), event);
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
        } else if (doneRxLimit.isPresent()) {
          checkAndReturnDoneRxLimit(event, doneRxLimit.get());
          return;
        }

        if (event.getSize() <= 0l && doneLimit.isPresent()) {
          checkAndReturnFilledLimit(doneLimit.get());
        } else if (event.getSize() > 0l && !doneLimit.isPresent()) {
          throw new OrderEventException("order for cancel order event not found on the book");
        } else if (event.getSize() > 0l && doneLimit.isPresent()) {
          checkAndReturnCanceledLimit(event, doneLimit.get());
        }
        break;
    }
  }

}
