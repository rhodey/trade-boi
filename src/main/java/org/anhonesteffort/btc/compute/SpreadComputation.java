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

package org.anhonesteffort.btc.compute;

import org.anhonesteffort.btc.state.State;

import java.util.Optional;

public class SpreadComputation extends Computation<Optional<Long>> {

  private final BestAskComputation ask = new BestAskComputation();
  private final BestBidComputation bid = new BestBidComputation();

  public SpreadComputation() {
    addChildren(ask, bid);
  }

  @Override
  protected Optional<Long> computeNextResult(State state) {
    if (ask.getResult().isPresent() && bid.getResult().isPresent()) {
      return Optional.of(
          ask.getResult().get().getPrice() - bid.getResult().get().getPrice()
      );
    } else {
      return Optional.empty();
    }
  }

}
