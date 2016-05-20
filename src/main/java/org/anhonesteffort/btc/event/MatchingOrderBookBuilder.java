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
    taker market ask
    maker limit bid
   */

  @Override
  protected void onEvent(OrderEvent event) throws OrderEventException {
    super.onEvent(event);
    if (!event.getType().equals(OrderEvent.Type.MATCH)) { return; }

    Order      taker    = takePooledTakerOrder(event);
    TakeResult result   = book.add(taker);

    if (!isEqual(result.getTakeSize(), event.getSize())) {
      log.error("taker order " + taker.getOrderId() + " side " + taker.getSide() + " price " + taker.getPrice() + " size " + taker.getSize());
      log.error("maker order " + event.getMakerId() + " side " + event.getSide() + " price " + event.getPrice() + " size " + event.getSize());

      if (taker instanceof MarketOrder) {
        log.error("taker was market order with remaining " + ((MarketOrder) taker).getSizeRemainingFor(event.getPrice()));

        if (taker.getSide().equals(Order.Side.BID)) {
          Order bestAsk = book.getAskLimits().peek().get().peek().get();
          log.error("best maker ask on the book is " + bestAsk.getOrderId() + " side " + bestAsk.getSide() + " price " + bestAsk.getPrice() + " size " + bestAsk.getSizeRemaining());
        } else {
          Order bestBid = book.getBidLimits().peek().get().peek().get();
          log.error("best maker bid on the book is " + bestBid.getOrderId() + " side " + bestBid.getSide() + " price " + bestBid.getPrice() + " size " + bestBid.getSizeRemaining());
        }
      } else {
        log.error("taker was limit order with remaining " + taker.getSizeRemaining());
      }

      throw new OrderEventException(
          "take size for match event does not agree with our book " +
              event.getSize() + " vs " + result.getTakeSize()
      );
    } else if (taker.getSizeRemaining() <= 0) {
      onOrderMatched(taker, result);
      returnPooledOrder(taker);
      returnPooledOrders(result);
    } else if (isEqual(taker.getSizeRemaining(), 0)) {
      if (book.remove(taker.getSide(), event.getPrice(), taker.getOrderId()).isPresent()) {
        onOrderMatched(taker, result);
        returnPooledOrder(taker);
        returnPooledOrders(result);
      } else {
        throw new OrderEventException("taker for match event has size remaining but not found in book");
      }
    } else {
      throw new OrderEventException("taker for match event was left on the book with " + taker.getSizeRemaining());
    }
  }

  protected void onOrderMatched(Order taker, TakeResult result) {
    log.info("matched order " + taker.getOrderId() + " for size " + result.getTakeSize());
  }

}
