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

package org.anhonesteffort.trading.strategy;

import org.anhonesteffort.trading.http.request.RequestFactory;
import org.anhonesteffort.trading.http.request.model.PostOrderRequest;
import org.anhonesteffort.trading.state.GdaxState;
import org.anhonesteffort.trading.state.StateProcessingException;
import org.anhonesteffort.trading.book.Order;

import java.util.Optional;

public abstract class BidIdentifyingStrategy extends Strategy<Optional<PostOrderRequest>> {

  private final RequestFactory requests;

  public BidIdentifyingStrategy(RequestFactory requests) {
    this.requests = requests;
  }

  protected PostOrderRequest bidRequest(double price, double size) {
    return requests.newOrder(Order.Side.BID, price, size);
  }

  protected abstract Optional<PostOrderRequest> identifyBid(GdaxState state, long nanoseconds)
      throws StateProcessingException;

  @Override
  protected Optional<PostOrderRequest> advanceStrategy(GdaxState state, long nanoseconds)
      throws StateProcessingException
  {
    if (isSyncing()) {
      return Optional.empty();
    } else {
      return identifyBid(state, nanoseconds);
    }
  }

}
