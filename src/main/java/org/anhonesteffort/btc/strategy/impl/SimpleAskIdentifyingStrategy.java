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

package org.anhonesteffort.btc.strategy.impl;

import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.http.request.RequestFactory;
import org.anhonesteffort.btc.state.GdaxState;
import org.anhonesteffort.btc.strategy.AskIdentifyingStrategy;
import org.anhonesteffort.btc.util.LongCaster;

import java.util.Optional;

public class SimpleAskIdentifyingStrategy extends AskIdentifyingStrategy {

  public SimpleAskIdentifyingStrategy(LongCaster caster, RequestFactory requests) {
    super(caster, requests);
  }

  @Override
  protected Optional<Double> identifyPrice(
      Order bidPosition, Optional<Order> lastAsk, GdaxState state, long nanoseconds
  ) {
    double bidFloor   = caster.toDouble(state.getOrderBook().getBidLimits().peek().get().getPrice());
    double askCeiling = caster.toDouble(state.getOrderBook().getAskLimits().peek().get().getPrice());
    double lastPrice  = lastAsk.isPresent() ? caster.toDouble(lastAsk.get().getPrice()) : -1l;
    double nextPrice  = (lastPrice - 0.01d);
    double bidPrice   = caster.toDouble(bidPosition.getPrice());

    if (!lastAsk.isPresent()) {
      return Optional.of(askCeiling);
    } else if (nextPrice > bidFloor && nextPrice > bidPrice) {
      return Optional.of(nextPrice);
    } else if (bidPrice > bidFloor) {
      return Optional.of(bidPrice);
    } else {
      return Optional.of(bidFloor + 0.01d);
    }
  }

}
