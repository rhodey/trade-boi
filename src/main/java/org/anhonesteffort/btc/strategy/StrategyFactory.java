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

package org.anhonesteffort.btc.strategy;

import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.http.HttpClientWrapper;
import org.anhonesteffort.btc.http.request.model.PostOrderRequest;

public abstract class StrategyFactory {

  private final HttpClientWrapper http;

  public StrategyFactory(HttpClientWrapper http) {
    this.http = http;
  }

  public abstract BidIdentifyingStrategy newBidIdentifying();

  public abstract AskIdentifyingStrategy newAskIdentifying();

  public abstract OrderMatchingStrategy newOrderMatching(Order.Side side, String orderId);

  public OrderOpeningStrategy newOrderOpening(PostOrderRequest order) {
    return new OrderOpeningStrategy(http, order);
  }

  public OrderCancelingStrategy newOrderCanceling(String orderId) {
    return new OrderCancelingStrategy(http, orderId);
  }

}