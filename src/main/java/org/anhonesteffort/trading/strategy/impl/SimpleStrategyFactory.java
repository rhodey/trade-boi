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

package org.anhonesteffort.trading.strategy.impl;

import org.anhonesteffort.trading.http.HttpClientWrapper;
import org.anhonesteffort.trading.http.request.RequestFactory;
import org.anhonesteffort.trading.strategy.AskIdentifyingStrategy;
import org.anhonesteffort.trading.strategy.BidIdentifyingStrategy;
import org.anhonesteffort.trading.strategy.OrderMatchingStrategy;
import org.anhonesteffort.trading.strategy.StrategyFactory;
import org.anhonesteffort.trading.book.Order;

public class SimpleStrategyFactory extends StrategyFactory {

  private static final Double BID_SIZE             = 0.01d;
  private static final Double BID_PLACEMENT        = 0.75d;
  private static final Long   RECENT_MS            = 30_000l;
  private static final Double RECENT_BUY_RATIO_MIN = 1.25d;
  private static final Double RECENT_BUY_BTC_MIN   = 1.50d;
  private static final Long   NOW_MS               = 5_000l;

  private static final Long BID_ABORT_MS = 12_000l;
  private static final Long ASK_ABORT_MS =  2_250l;

  private final RequestFactory requests = new RequestFactory();

  public SimpleStrategyFactory(HttpClientWrapper http) {
    super(http);
  }

  private SimpleBidIdentifyingStrategy.Params bidParams() {
    return new SimpleBidIdentifyingStrategy.Params(
        BID_SIZE, BID_PLACEMENT, RECENT_MS, RECENT_BUY_RATIO_MIN, RECENT_BUY_BTC_MIN, NOW_MS
    );
  }

  @Override
  public BidIdentifyingStrategy newBidIdentifying() {
    return new SimpleBidIdentifyingStrategy(bidParams(), requests);
  }

  @Override
  public OrderMatchingStrategy newOrderMatching(Order.Side side, String orderId) {
    if (side == Order.Side.BID) {
      return new SimpleOrderMatchingStrategy(orderId, BID_ABORT_MS);
    } else {
      return new SimpleOrderMatchingStrategy(orderId, ASK_ABORT_MS);
    }
  }

  @Override
  public AskIdentifyingStrategy newAskIdentifying() {
    return new SimpleAskIdentifyingStrategy(requests);
  }

}
