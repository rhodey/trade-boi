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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarketOrderStateCurator extends LimitOrderStateCurator {

  private static final Logger log = LoggerFactory.getLogger(MarketOrderStateCurator.class);

  public MarketOrderStateCurator(LimitOrderBook book, OrderPool pool) {
    super(book, pool);
  }

  private MarketOrder takePooledMarketOrder(OrderEvent marketRx) throws OrderEventException {
    if (marketRx.getSize() < 0l || marketRx.getFunds() < 0l) {
      throw new OrderEventException("market order rx event was parsed incorrectly");
    } else if (marketRx.getSize() > 0l || marketRx.getFunds() > 0l) {
      return pool.takeMarket(marketRx.getOrderId(), marketRx.getSide(), marketRx.getSize(), marketRx.getFunds());
    } else {
      throw new OrderEventException("market order rx event has no size or funds");
    }
  }

  private MarketOrder takePooledMarketOrderChange(OrderEvent change) throws OrderEventException {
    return pool.takeMarket(change.getOrderId(), change.getSide(), change.getNewSize(), change.getNewFunds());
  }

  @Override
  protected void onEvent(OrderEvent event) throws OrderEventException {
    super.onEvent(event);
    switch (event.getType()) {
      case MARKET_RX:
        onMarketOrderReceived(takePooledMarketOrder(event));
        break;

      case MARKET_CHANGE:
        if (event.getNewSize() < 0l || event.getNewFunds() < 0l) {
          throw new OrderEventException("market order change event was parsed incorrectly");
        } else if (event.getNewSize() > event.getOldSize() || event.getNewFunds() > event.getOldFunds()) {
          throw new OrderEventException("market order size and funds can only decrease");
        }

        long sizeReduced  = event.getOldSize()  - event.getNewSize();
        long fundsReduced = event.getOldFunds() - event.getNewFunds();

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
    if (state.getMarketOrders().put(order.getOrderId(), order) != null) {
      throw new OrderEventException("market order " + order.getOrderId() + " already in the active set");
    } else {
      log.debug("received new market order " + order.getOrderId());
    }
  }

  protected void onMarketOrderChange(OrderEvent event, long sizeReduced, long fundsReduced) {
    log.warn("!!! changed market order " + event.getOrderId() + " by " + sizeReduced + " and " + fundsReduced + " !!!");
  }

  protected void onMarketOrderDone(String orderId, Order.Side side) throws OrderEventException {
    MarketOrder order = state.getMarketOrders().remove(orderId);
    if (order == null) {
      throw new OrderEventException("market order " + orderId + " was never in the active set");
    } else {
      returnPooledOrder(order);
      log.debug("market order done " + orderId);
    }
  }

}
