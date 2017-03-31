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

package org.anhonesteffort.trading.compute;

import org.anhonesteffort.trading.state.GdaxState;
import org.anhonesteffort.trading.book.Limit;

import java.util.Optional;

public class SpreadComputation extends Computation<Optional<Double>> {

  @Override
  protected Optional<Double> computeNextResult(GdaxState state, long nanoseconds) {
    Optional<Limit> ask = state.getOrderBook().getAskLimits().peek();
    Optional<Limit> bid = state.getOrderBook().getBidLimits().peek();

    if (ask.isPresent() && bid.isPresent()) {
      return Optional.of(ask.get().getPrice() - bid.get().getPrice());
    } else {
      return Optional.empty();
    }
  }

}
