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
import org.anhonesteffort.btc.book.OrderPool;
import org.anhonesteffort.btc.compute.Computation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

public class MarketOrderStateCurator extends LimitOrderStateCurator {

  private static final Logger log = LoggerFactory.getLogger(MarketOrderStateCurator.class);

  public MarketOrderStateCurator(LimitOrderBook book, OrderPool pool, Set<Computation> computations) {
    super(book, pool, computations);
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

  private MarketOrder takePooledMarketOrderChange(OrderEvent change) {
    return pool.takeMarket(change.getOrderId(), change.getSide(), change.getNewSize(), change.getNewFunds());
  }

  @Override
  protected void onEvent(OrderEvent event) throws OrderEventException {
    super.onEvent(event);
    switch (event.getType()) {
      case MARKET_RX:
        MarketOrder rxMarket = takePooledMarketOrder(event);
        if (state.getMarketOrders().put(rxMarket.getOrderId(), rxMarket) != null) {
          throw new OrderEventException("market order " + rxMarket.getOrderId() + " already in the market state map");
        } else {
          onMarketOrderRx(rxMarket);
        }
        break;

      case MARKET_CHANGE:
        if (event.getNewSize() < 0l || event.getNewFunds() < 0l) {
          throw new OrderEventException("market order change event was parsed incorrectly");
        } else if (event.getNewSize() > event.getOldSize() || event.getNewFunds() > event.getOldFunds()) {
          throw new OrderEventException("market order size and funds can only decrease");
        }

        long                  sizeReduced  = event.getOldSize()  - event.getNewSize();
        long                  fundsReduced = event.getOldFunds() - event.getNewFunds();
        Optional<MarketOrder> changeMarket = Optional.ofNullable(
            state.getMarketOrders().remove(event.getOrderId())
        );

        if (!changeMarket.isPresent()) {
          throw new OrderEventException("market order for change event not found in the market state map");
        } else {
          MarketOrder newMarket = takePooledMarketOrderChange(event);
          state.getMarketOrders().put(newMarket.getOrderId(), newMarket);
          onMarketOrderChange(newMarket, sizeReduced, fundsReduced);
          returnPooledOrder(changeMarket.get());
        }
        break;

      case MARKET_DONE:
        Optional<MarketOrder> doneMarket = Optional.ofNullable(
            state.getMarketOrders().remove(event.getOrderId())
        );

        if (!doneMarket.isPresent()) {
          throw new OrderEventException("market order " + event.getOrderId() + " was never in the market state map");
        } else {
          onMarketOrderDone(doneMarket.get());
          returnPooledOrder(doneMarket.get());
        }
        break;
    }
  }

  protected void onMarketOrderRx(MarketOrder order) {
    log.debug("received new market order " + order.getOrderId());
  }

  protected void onMarketOrderChange(MarketOrder order, long sizeReduced, long fundsReduced) {
    log.warn("!!! changed market order " + order.getOrderId() + " by " + sizeReduced + " and " + fundsReduced + " !!!");
  }

  protected void onMarketOrderDone(MarketOrder order) {
    log.debug("market order done " + order.getOrderId());
  }

}
