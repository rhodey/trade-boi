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

import java.util.Set;

public class MarketOrderStateCurator extends LimitOrderStateCurator {

  /*
  coinbase lies about market orders in receive messages
  https://community.coinbase.com/t/why-is-this-market-order-filled-without-having-any-fills/10001
   */
  public MarketOrderStateCurator(CompatLimitOrderBook book, Set<StateListener> listeners) {
    super(book, listeners);
  }

  @Override
  protected void onEvent(GdaxEvent event) throws StateProcessingException {
    super.onEvent(event);
    switch (event.getType()) {
      case MARKET_RX:
        if (!state.getMarketOrderIds().add(event.getOrderId())) {
          throw new StateProcessingException("market order " + event.getOrderId() + " already in the market state map");
        }
        break;

      case MARKET_DONE:
        if (!state.getMarketOrderIds().remove(event.getOrderId())) {
          throw new StateProcessingException("market order " + event.getOrderId() + " was never in the market state map");
        }
        break;
    }
  }

}
