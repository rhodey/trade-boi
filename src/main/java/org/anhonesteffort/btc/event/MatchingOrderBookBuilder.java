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
import org.anhonesteffort.btc.book.MarketOrder;
import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.book.OrderPool;
import org.anhonesteffort.btc.book.TakeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class MatchingOrderBookBuilder extends MarketOrderBookBuilder {

  private static final Logger log = LoggerFactory.getLogger(MatchingOrderBookBuilder.class);

  public MatchingOrderBookBuilder(HeuristicLimitOrderBook book, OrderPool pool) {
    super(book, pool);
  }

  protected Order takePooledTakerOrder(OrderEvent match) throws OrderEventException {
    if (match.getPrice() > 0 && match.getSize() > 0) {
      if (match.getSide().equals(Order.Side.ASK)) {
        if (!activeMarketOrders.contains(match.getTakerId())) {
          return pool.take(match.getTakerId(), Order.Side.BID, match.getPrice(), match.getSize());
        } else {
          return pool.takeMarket(match.getTakerId(), Order.Side.BID, match.getSize(), -1);
        }
      } else {
        if (!activeMarketOrders.contains(match.getTakerId())) {
          return pool.take(match.getTakerId(), Order.Side.ASK, match.getPrice(), match.getSize());
        } else {
          return pool.takeMarket(match.getTakerId(), Order.Side.ASK, match.getSize(), -1);
        }
      }
    } else {
      throw new OrderEventException("match event has invalid taker price or size");
    }
  }

  /*
  1. received market bid order for size 1 with funds -1 and price 0
  2. limit ask order for size 1 price 10 is on the book
  3. adding market bid to book does not take limit ask
   */

  @Override
  protected void onEvent(OrderEvent event) throws OrderEventException {
    super.onEvent(event);
    if (!event.getType().equals(OrderEvent.Type.MATCH)) { return; }

    Order      taker    = takePooledTakerOrder(event); // market bid order for 0.01btc at $0.0
    TakeResult result   = book.add(taker);

    if (!isEqual(result.getTakeSize(), event.getSize())) {
      log.error("taker order side " + taker.getSide() + ", price " + taker.getPrice() + ", size " + taker.getSize() + ", remaining " + taker.getSizeRemaining());
      log.error("maker order side " + event.getSide() + ", price " + event.getPrice() + ", size " + event.getSize());

      if (taker instanceof MarketOrder) {
        log.error("taker was market order");
        Order      lol = new Order(9001, "lol wut", taker.getSide(), event.getPrice(), taker.getSize());
        TakeResult wut = book.add(lol);

        if (wut.getTakeSize() > 0) {
          log.error("mock limit taker took " + wut.getTakeSize() + " from " + wut.getMakers().get(0).getOrderId());
        } else {
          log.error("mock limit taker did not take, as it should have");
        }
      } else {
        Optional<Order> maker = book.remove(event.getSide(), event.getPrice(), event.getMakerId());
        if (maker.isPresent()) {
          log.error("maker is still on the book with remaining " + maker.get().getSizeRemaining());
        } else {
          log.error("maker was not on the book");
        }
      }

      throw new OrderEventException(
          "take size for match event does not agree with our book " +
              event.getSize() + " vs " + result.getTakeSize()
      );
    } else if (taker.getSizeRemaining() > 0) {
      throw new OrderEventException("taker for match event was left on the book with " + taker.getSizeRemaining());
    } else {
      onOrderMatched(taker, result);
      returnPooledOrder(taker);
      returnPooledOrders(result);
    }
  }

  protected void onOrderMatched(Order taker, TakeResult result) {
    log.info("matched order " + taker.getOrderId() + " for size " + result.getTakeSize());
  }

}
