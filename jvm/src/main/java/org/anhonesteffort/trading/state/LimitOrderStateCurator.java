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

package org.anhonesteffort.trading.state;

import org.anhonesteffort.trading.book.CompatLimitOrderBook;
import org.anhonesteffort.trading.book.CompatTakeResult;
import org.anhonesteffort.trading.book.Orders.Order;

import java.util.Optional;
import java.util.Set;

public class LimitOrderStateCurator extends StateCurator {

  public LimitOrderStateCurator(CompatLimitOrderBook book, Set<StateListener> listeners) {
    super(book, listeners);
  }

  private Order newLimitOrderForEvent(GdaxEvent event) throws StateProcessingException {
    if (event.getPrice() > 0d && event.getSize() > 0d) {
      return new Order(event.getOrderId(), event.getSide(), event.getPrice(), event.getSize());
    } else {
      throw new StateProcessingException("limit order rx/open event has invalid price or size");
    }
  }

  private void checkRxLimitOrderForOpen(GdaxEvent open) throws StateProcessingException {
    Optional<Order> rxLimit = Optional.ofNullable(state.getRxLimitOrders().remove(open.getOrderId()));
    if (!rxLimit.isPresent() && !isSyncing()) {
      throw new StateProcessingException("limit order " + open.getOrderId() + " was never in the limit rx state map");
    } else if (rxLimit.isPresent() && Math.abs(rxLimit.get().getSizeRemaining() - open.getSize()) > FORGIVE_SIZE) {
      throw new StateProcessingException(
          "rx limit order for limit open event disagrees about open size, " +
              "event wants " + open.getSize() + ", rx has " + rxLimit.get().getSizeRemaining()
      );
    }
  }

  private double getSizeReducedForChange(GdaxEvent change) throws StateProcessingException {
    if (change.getNewSize() >= change.getOldSize()) {
      throw new StateProcessingException("limit order size can only decrease");
    } else {
      return change.getOldSize() - change.getNewSize();
    }
  }

  private Order newRxLimitOrderChange(Order rxLimit, GdaxEvent change) throws StateProcessingException {
    if (change.getNewSize() >= rxLimit.getSize()) {
      throw new StateProcessingException("limit order change event new size is >= rx limit order size");
    } else {
      return new Order(change.getOrderId(), change.getSide(), change.getPrice(), change.getNewSize());
    }
  }

  private void checkDoneRxLimitOrder(GdaxEvent done, Order rxLimit) throws StateProcessingException {
    if (Math.abs(rxLimit.getSizeRemaining() - done.getSize()) > FORGIVE_SIZE) {
      throw new StateProcessingException(
          "rx limit order for limit done event disagrees about size remaining, " +
              "event wants " + done.getSize() + ", rx has " + rxLimit.getSizeRemaining()
      );
    }
  }

  private void checkFilledLimitOrder(Order fillLimit) throws StateProcessingException {
    if (fillLimit.getSizeRemaining() > FORGIVE_SIZE) {
      throw new StateProcessingException("order for filled order event was still open on the book with " + fillLimit.getSizeRemaining());
    }
  }

  private void checkCanceledLimitOrder(GdaxEvent done, Order cancelLimit) throws StateProcessingException {
    if (Math.abs(done.getSize() - cancelLimit.getSizeRemaining()) > FORGIVE_SIZE) {
      throw new StateProcessingException(
          "order for cancel order event disagrees about size remaining, " +
              "event wants " + done.getSize() + ", order has " + cancelLimit.getSizeRemaining()
      );
    }
  }

  @Override
  protected void onEvent(GdaxEvent event) throws StateProcessingException {
    switch (event.getType()) {
      case LIMIT_RX:
        Order rxOrder = newLimitOrderForEvent(event);
        if (state.getRxLimitOrders().put(rxOrder.getOrderId(), rxOrder) != null) {
          throw new StateProcessingException("limit order " + rxOrder.getOrderId() + " already in the limit rx state map");
        } else if (event.getClientOid() != null) {
          state.getClientOIdMap().put(event.getClientOid(), event.getOrderId());
        }
        break;

      case LIMIT_OPEN:
        checkRxLimitOrderForOpen(event);
        Order            openOrder = newLimitOrderForEvent(event);
        CompatTakeResult result    = state.getOrderBook().jadd(openOrder);
        if (result.getTakeSize() > 0d) {
          throw new StateProcessingException("opened limit order took " + result.getTakeSize() + " from the book");
        } else {
          state.setEvent(Events.open(openOrder, event.getNanoseconds()));
        }
        break;

      case LIMIT_CHANGE:
        double          reducedBy        = getSizeReducedForChange(event);
        Optional<Order> changedRxOrder   = Optional.ofNullable(state.getRxLimitOrders().remove(event.getOrderId()));
        Optional<Order> changedOpenOrder = state.getOrderBook().jreduce(event.getSide(), event.getPrice(), event.getOrderId(), reducedBy);

        if (changedRxOrder.isPresent() && changedOpenOrder.isPresent()) {
          throw new StateProcessingException("order for limit change event was in the limit rx state map and open on the book");
        } else if (changedRxOrder.isPresent()) {
          Order newRxLimit = newRxLimitOrderChange(changedRxOrder.get(), event);
          state.getRxLimitOrders().put(newRxLimit.getOrderId(), newRxLimit);
        } else if (!changedOpenOrder.isPresent()) {
          throw new StateProcessingException("order for limit change event not found on the book");
        } else {
          state.setEvent(Events.reduce(changedOpenOrder.get(), reducedBy, event.getNanoseconds()));
        }
        break;

      case LIMIT_DONE:
        Optional<Order> doneRxOrder   = Optional.ofNullable(state.getRxLimitOrders().remove(event.getOrderId()));
        Optional<Order> doneOpenOrder = state.getOrderBook().jremove(event.getSide(), event.getPrice(), event.getOrderId());

        if (doneRxOrder.isPresent() && doneOpenOrder.isPresent()) {
          throw new StateProcessingException("order for limit done event was in the limit rx state map and open on the book");
        } else if (doneRxOrder.isPresent()) {
          checkDoneRxLimitOrder(event, doneRxOrder.get());
          return;
        }

        if (event.getSize() <= 0d && doneOpenOrder.isPresent()) {
          checkFilledLimitOrder(doneOpenOrder.get());
        } else if (event.getSize() > 0d && !doneOpenOrder.isPresent()) {
          throw new StateProcessingException("order for cancel order event not found on the book");
        } else if (event.getSize() > 0d && doneOpenOrder.isPresent()) {
          checkCanceledLimitOrder(event, doneOpenOrder.get());
          state.setEvent(Events.cancel(doneOpenOrder.get(), event.getNanoseconds()));
        }
        break;
    }
  }

}
