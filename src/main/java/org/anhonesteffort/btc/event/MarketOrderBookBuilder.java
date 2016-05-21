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
import org.anhonesteffort.btc.util.LongCaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class MarketOrderBookBuilder extends LimitOrderBookBuilder {

  private static final Logger log = LoggerFactory.getLogger(MarketOrderBookBuilder.class);
  protected final Set<String> activeMarketOrders = new HashSet<>();

  public MarketOrderBookBuilder(HeuristicLimitOrderBook book, OrderPool pool, LongCaster caster) {
    super(book, pool, caster);
  }

  protected MarketOrder takePooledMarketOrder(OrderEvent marketRx) throws OrderEventException {
    if (marketRx.getSize() < 0 || marketRx.getFunds() < 0) {
      throw new OrderEventException("market order rx event was parsed incorrectly");
    } else if (marketRx.getSize() > 0 || marketRx.getFunds() > 0) {
      return pool.takeMarket(marketRx.getOrderId(), marketRx.getSide(), marketRx.getSize(), marketRx.getFunds());
    } else {
      throw new OrderEventException("market order rx event has no size or funds");
    }
  }

  protected MarketOrder takePooledMarketOrderChange(OrderEvent change) throws OrderEventException {
    return pool.takeMarket(change.getOrderId(), change.getSide(), change.getNewSize(), change.getNewFunds());
  }

  @Override
  protected void onEvent(OrderEvent event) throws OrderEventException {
    super.onEvent(event);
    switch (event.getType()) {
      case MARKET_RX:
        MarketOrder marketRx = takePooledMarketOrder(event);
        onMarketOrderReceived(marketRx);
        returnPooledOrder(marketRx);
        break;

      case MARKET_CHANGE:
        if (event.getNewSize() < 0 || event.getNewFunds() < 0) {
          throw new OrderEventException("market order change event was parsed incorrectly");
        } else if (event.getNewSize() > event.getOldSize() || event.getNewFunds() > event.getOldFunds()) {
          throw new OrderEventException("market order size and funds can only decrease");
        }

        double sizeReduced  = event.getOldSize()  - event.getNewSize();
        double fundsReduced = event.getOldFunds() - event.getNewFunds();

        MarketOrder marketChange = takePooledMarketOrderChange(event);
        onMarketOrderChange(event, sizeReduced, fundsReduced);
        returnPooledOrder(marketChange);
        break;

      case MARKET_DONE:
        onMarketOrderDone(event.getOrderId(), event.getSide());
        break;
    }
  }

  protected void onMarketOrderReceived(MarketOrder order) throws OrderEventException {
    if (!activeMarketOrders.add(order.getOrderId())) {
      throw new OrderEventException("market order " + order.getOrderId() + " already in the active set");
    } else {
      log.info("received new market order " + order.getOrderId());
    }
  }

  protected void onMarketOrderChange(OrderEvent event, double sizeReduced, double fundsReduced) {
    log.warn("!!! changed market order " + event.getOrderId() + " by " + sizeReduced + " and " + fundsReduced + " !!!");
  }

  protected void onMarketOrderDone(String orderId, Order.Side side) throws OrderEventException {
    if (!activeMarketOrders.remove(orderId)) {
      throw new OrderEventException("market order " + orderId + " was never in the active set");
    } else {
      log.info("market order done " + orderId);
    }
  }

}
