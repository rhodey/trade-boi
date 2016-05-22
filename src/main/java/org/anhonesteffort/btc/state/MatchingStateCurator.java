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
import org.anhonesteffort.btc.book.MarketOrder;
import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.book.OrderPool;
import org.anhonesteffort.btc.book.TakeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class MatchingStateCurator extends MarketOrderStateCurator {

  private static final Logger log = LoggerFactory.getLogger(MatchingStateCurator.class);

  public MatchingStateCurator(LimitOrderBook book, OrderPool pool) {
    super(book, pool);
  }

  private Order takePooledTakerOrder(OrderEvent match) throws OrderEventException {
    if (match.getPrice() > 0l && match.getSize() > 0l) {
      if (match.getSide().equals(Order.Side.ASK)) {
        if (!state.getMarketOrders().containsKey(match.getTakerId())) {
          return pool.take(match.getTakerId(), Order.Side.BID, match.getPrice(), match.getSize());
        } else {
          return pool.takeMarket(match.getTakerId(), Order.Side.BID, match.getSize(), -1l);
        }
      } else {
        if (!state.getMarketOrders().containsKey(match.getTakerId())) {
          return pool.take(match.getTakerId(), Order.Side.ASK, match.getPrice(), match.getSize());
        } else {
          return pool.takeMarket(match.getTakerId(), Order.Side.ASK, match.getSize(), -1l);
        }
      }
    } else {
      throw new OrderEventException("match event has invalid taker price or size");
    }
  }

  @Override
  protected void onEvent(OrderEvent event) throws OrderEventException {
    super.onEvent(event);
    if (!event.getType().equals(OrderEvent.Type.MATCH)) { return; }

    Order      taker    = takePooledTakerOrder(event);
    TakeResult result   = state.getOrderBook().add(taker);

    if (result.getTakeSize() != event.getSize()) {
      log.error("taker order " + taker.getOrderId() + " side " + taker.getSide() + " price " + taker.getPrice() + " size " + taker.getSize());
      log.error("maker order " + event.getMakerId() + " side " + event.getSide() + " price " + event.getPrice() + " size " + event.getSize());

      throw new OrderEventException(
          "take size for match event does not agree with our book " +
              event.getSize() + " vs " + result.getTakeSize()
      );
    } else if (taker.getSizeRemaining() > 0l) {
      throw new OrderEventException("taker for match event was left on the book with " + taker.getSizeRemaining());
    } else if (taker instanceof MarketOrder) {
      Optional<MarketOrder> marketTaker = Optional.ofNullable(state.getMarketOrders().get(taker.getOrderId()));
      if (!marketTaker.isPresent()) {
        throw new OrderEventException("market order for match event not found in the market state map");
      } else {
        // todo: update rx market
        onOrderMatched(taker, result);
        returnPooledOrder(taker);
        returnPooledOrders(result);
      }
    } else {
      Optional<Order> limitTaker = Optional.ofNullable(state.getRxLimitOrders().get(taker.getOrderId()));
      if (!limitTaker.isPresent()) {
        throw new OrderEventException("limit order for match event not found in the limit state map");
      } else {
        // todo: update rx limit
        onOrderMatched(taker, result);
        returnPooledOrder(taker);
        returnPooledOrders(result);
      }
    }
  }

  protected void onOrderMatched(Order taker, TakeResult result) {
    log.debug("matched order " + taker.getOrderId() + " for size " + result.getTakeSize());
  }

}
