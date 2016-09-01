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
import org.anhonesteffort.btc.http.request.RequestFactory;
import org.anhonesteffort.btc.http.request.model.PostOrderRequest;
import org.anhonesteffort.btc.state.State;
import org.anhonesteffort.btc.util.LongCaster;

import java.util.Optional;

public class AskIdentifyingStrategy extends Strategy<Optional<PostOrderRequest>> {

  private final RequestFactory requests = new RequestFactory();

  private final LongCaster      caster;
  private final Order           bidPosition;
  private final Optional<Order> lastAsk;

  public AskIdentifyingStrategy(LongCaster caster, Order bidPosition, Optional<Order> lastAsk) {
    this.caster      = caster;
    this.bidPosition = bidPosition;
    this.lastAsk     = lastAsk;
  }

  @Override
  protected Optional<PostOrderRequest> advanceStrategy(State state, long nanoseconds) {
    double bidFloor   = caster.toDouble(state.getOrderBook().getBidLimits().peek().get().getPrice());
    double askCeiling = caster.toDouble(state.getOrderBook().getAskLimits().peek().get().getPrice());
    double lastPrice  = lastAsk.isPresent() ? caster.toDouble(lastAsk.get().getPrice()) : -1l;
    double askSize    = caster.toDouble(bidPosition.getSize());
    double minPrice   = bidFloor + 0.01d;

    if (!lastAsk.isPresent()) {
      return Optional.of(requests.newOrder(Order.Side.ASK, askCeiling, askSize));
    } else if (lastPrice > minPrice) {
      return Optional.of(requests.newOrder(Order.Side.ASK, (lastPrice - 0.01d), askSize));
    } else {
      return Optional.of(requests.newOrder(Order.Side.ASK, minPrice, askSize));
    }
  }

}
