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

public class MatchingStateCurator extends MarketOrderStateCurator {

  /*
  todo:
    if coinbase receives any takers or does any matching while we're rebuilding,
    and the matching does not complete until after we've finished rebuilding,
    we'll be unable to find takers for the remaining match events.

    we can only really begin to trust the rx limit and market state after some
    time has passed since rebuilding
   */
  public MatchingStateCurator(LimitOrderBook book, OrderPool pool, Set<Computation> computations) {
    super(book, pool, computations);
  }

  private Order takePooledTakerOrder(OrderEvent match) throws OrderEventException {
    if (match.getPrice() > 0l && match.getSize() > 0l) {
      if (match.getSide().equals(Order.Side.ASK)) {
        return pool.take(match.getTakerId(), Order.Side.BID, match.getPrice(), match.getSize());
      } else {
        return pool.take(match.getTakerId(), Order.Side.ASK, match.getPrice(), match.getSize());
      }
    } else {
      throw new OrderEventException("match event has invalid taker price or size");
    }
  }

  @Override
  protected void onEvent(OrderEvent event) throws OrderEventException {
    super.onEvent(event);
    if (!event.getType().equals(OrderEvent.Type.MATCH)) { return; }

    Order      taker  = takePooledTakerOrder(event);
    TakeResult result = state.getOrderBook().add(taker);

    if (Math.abs(result.getTakeSize() - event.getSize()) > 1l) {
      throw new OrderEventException(
          "take size for match event does not agree with our book " +
              event.getSize() + " vs " + result.getTakeSize()
      );
    } else if (taker.getSizeRemaining() > 0l) {
      throw new OrderEventException("taker for match event was left on the book with " + taker.getSizeRemaining());
    } else if (state.getMarketOrders().containsKey(taker.getOrderId())) {
      returnPooledOrder(taker);
      returnPooledOrders(result);
    } else {
      Optional<Order> limitTaker = Optional.ofNullable(state.getRxLimitOrders().get(taker.getOrderId()));
      if (!limitTaker.isPresent()) {
        throw new OrderEventException("limit order for match event not found in the limit rx state map");
      }

      long rxLimitTakeSize = limitTaker.get().takeSize(event.getSize());
      if (Math.abs(rxLimitTakeSize - event.getSize()) > 1l) {
        throw new OrderEventException(
            "limit order for match event disagrees with order size in the limit rx state map, " +
                "event wanted " + event.getSize() + ", state had " + rxLimitTakeSize
        );
      } else {
        returnPooledOrder(taker);
        returnPooledOrders(result);
      }
    }
  }

}
