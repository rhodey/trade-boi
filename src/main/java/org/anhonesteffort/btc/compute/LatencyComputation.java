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

import org.anhonesteffort.btc.state.GdaxState;

import java.util.Optional;

public class LatencyComputation extends Computation<Optional<Long>> {

  private final long mod;
  private long sequence = 0l;
  private long nanosecondSum = 0l;

  public LatencyComputation(long mod) {
    this.mod = mod;
  }

  public Long getMod() {
    return mod;
  }

  @Override
  protected Optional<Long> computeNextResult(GdaxState state, long nanoseconds) {
    if ((++sequence % mod) == 0l) {
      long copy = nanosecondSum;
      nanosecondSum = System.nanoTime() - nanoseconds;
      return Optional.of(copy / mod);
    } else {
      nanosecondSum += System.nanoTime() - nanoseconds;
      return Optional.empty();
    }
  }

}
