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
import org.anhonesteffort.btc.book.TakeResult;
import org.anhonesteffort.btc.compute.Computation;

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
  public MatchingStateCurator(LimitOrderBook book, Set<Computation> computations) {
    super(book, computations);
  }

  private Order takePooledTakerOrder(OrderEvent match) throws OrderEventException {
    if (match.getPrice() > 0l && match.getSize() > 0l) {
      if (match.getSide().equals(Order.Side.ASK)) {
        return new Order(match.getTakerId(), Order.Side.BID, match.getPrice(), match.getSize());
      } else {
        return new Order(match.getTakerId(), Order.Side.ASK, match.getPrice(), match.getSize());
      }
    } else {
      throw new OrderEventException("match event has invalid taker price or size");
    }
  }

  private void checkEventAgainstTakeResult(OrderEvent match, Order taker, TakeResult result) throws OrderEventException {
    if (Math.abs(result.getTakeSize() - match.getSize()) > 1l) {
      throw new OrderEventException(
          "take size for match event does not agree with our book, " +
              "event wants " + match.getSize() + ", book gave " + result.getTakeSize()
      );
    } else if (taker.getSizeRemaining() > 1l) {
      throw new OrderEventException("taker for match event was left on the book with " + taker.getSizeRemaining());
    } else if (taker.getSizeRemaining() > 0l) {
      state.getOrderBook().remove(taker.getSide(), taker.getPrice(), taker.getOrderId());
    }
  }

  private void updateRxLimitOrder(String takerId, long takeSize) throws OrderEventException {
    Optional<Order> limitTaker = Optional.ofNullable(state.getRxLimitOrders().get(takerId));
    if (!limitTaker.isPresent()) {
      throw new OrderEventException("limit order for match event not found in the limit rx state map");
    } else if (Math.abs(limitTaker.get().takeSize(takeSize) - takeSize) > 1l) {
      throw new OrderEventException(
          "limit order for match event disagrees with order size in the limit rx state map"
      );
    }
  }

  @Override
  protected void onEvent(OrderEvent event) throws OrderEventException {
    super.onEvent(event);
    if (!event.getType().equals(OrderEvent.Type.MATCH)) { return; }

    Order      taker  = takePooledTakerOrder(event);
    TakeResult result = state.getOrderBook().add(taker);

    checkEventAgainstTakeResult(event, taker, result);

    if (state.getMarketOrders().containsKey(taker.getOrderId())) {
      state.getTakes().add(result);
    } else {
      updateRxLimitOrder(taker.getOrderId(), event.getSize());
      state.getTakes().add(result);
    }
  }

}
