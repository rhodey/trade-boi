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
import org.anhonesteffort.trading.book.Orders;
import org.anhonesteffort.trading.book.Orders.Order;

import java.util.Optional;
import java.util.Set;

public class MatchingStateCurator extends MarketOrderStateCurator {

  /*
  todo:
    if coinbase receives any takers or does any matching while we're rebuilding,
    and the matching does not complete until after we've finished rebuilding,
    we'll be unable to find takers for the remaining match events.

    we can only really begin to trust the rx limit and market state after some
    time has passed since rebuilding
   */
  public MatchingStateCurator(CompatLimitOrderBook book, Set<StateListener> listeners) {
    super(book, listeners);
  }

  private Order newTakerOrder(GdaxEvent match) throws StateProcessingException {
    if (match.getPrice() > 0d && match.getSize() > 0d) {
      if (match.getSide() == Orders.SIDE_ASK()) {
        return Orders.limitBid(match.getTakerId(), match.getPrice(), match.getSize());
      } else {
        return Orders.limitAsk(match.getTakerId(), match.getPrice(), match.getSize());
      }
    } else {
      throw new StateProcessingException("match event has invalid taker price or size");
    }
  }

  private void checkEventAgainstTakeResult(GdaxEvent match, Order taker, CompatTakeResult result) throws StateProcessingException {
    if (Math.abs(result.getTakeSize() - match.getSize()) > FORGIVE_SIZE) {
      throw new StateProcessingException(
          "take size for match event does not agree with our book, " +
              "event wants " + match.getSize() + ", book gave " + result.getTakeSize()
      );
    } else if (taker.getSizeRemaining() > FORGIVE_SIZE) {
      throw new StateProcessingException("taker for match event was left on the book with " + taker.getSizeRemaining());
    } else if (taker.getSizeRemaining() > 0d) {
      state.getOrderBook().jremove(taker.getSide(), taker.getPrice(), taker.getOrderId());
    }
  }

  private void updateRxLimitOrder(String takerId, double takeSize) throws StateProcessingException {
    Optional<Order> limitTaker = Optional.ofNullable(state.getRxLimitOrders().get(takerId));
    if (!limitTaker.isPresent()) {
      throw new StateProcessingException("limit order for match event not found in the limit rx state map");
    } else if (Math.abs(limitTaker.get().takeSize(takeSize) - takeSize) > FORGIVE_SIZE) {
      throw new StateProcessingException(
          "limit order for match event disagrees with order size in the limit rx state map"
      );
    }
  }

  @Override
  protected void onEvent(GdaxEvent event) throws StateProcessingException {
    super.onEvent(event);
    if (!event.getType().equals(GdaxEvent.Type.MATCH)) { return; }

    Order            taker  = newTakerOrder(event);
    CompatTakeResult result = state.getOrderBook().jadd(taker);

    checkEventAgainstTakeResult(event, taker, result);

    if (!state.getMarketOrderIds().contains(taker.getOrderId())) {
      updateRxLimitOrder(taker.getOrderId(), event.getSize());
    }

    state.setEvent(Events.take(taker, event.getNanoseconds()));
    state.getMakers().addAll(result.getMakers());
  }

}
