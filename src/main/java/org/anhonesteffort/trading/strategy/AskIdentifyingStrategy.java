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
import org.anhonesteffort.trading.book.Order;
import org.anhonesteffort.trading.util.LongCaster;

import java.util.Optional;

public abstract class AskIdentifyingStrategy extends Strategy<Optional<PostOrderRequest>> {

  protected final LongCaster caster;
  private final RequestFactory requests;

  private Optional<Order> lastAsk     = Optional.empty();
  private Optional<Order> bidPosition = Optional.empty();

  public AskIdentifyingStrategy(LongCaster caster, RequestFactory requests) {
    this.caster   = caster;
    this.requests = requests;
  }

  public void setContext(Optional<Order> bidPosition, Optional<Order> lastAsk) {
    this.bidPosition = bidPosition;
    this.lastAsk     = lastAsk;
  }

  protected abstract Optional<Double> identifyPrice(
      Order bidPosition, Optional<Order> lastAsk, GdaxState state, long nanoseconds
  );

  @Override
  protected Optional<PostOrderRequest> advanceStrategy(GdaxState state, long nanoseconds) {
    if (isSyncing() || !bidPosition.isPresent()) {
      return Optional.empty();
    }

    Optional<Double> askPrice = identifyPrice(bidPosition.get(), lastAsk, state, nanoseconds);
    if (askPrice.isPresent()) {
      return Optional.of(requests.newOrder(
          Order.Side.ASK, askPrice.get(), caster.toDouble(bidPosition.get().getSize())
      ));
    } else {
      return Optional.empty();
    }

  }

}
