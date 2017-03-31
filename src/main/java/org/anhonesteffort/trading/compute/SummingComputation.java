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
import org.anhonesteffort.trading.state.StateProcessingException;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

public class SummingComputation extends Computation<Optional<Double>> {

  private final Queue<double[]> history = new ArrayDeque<>();
  private final Computation<Double> child;
  private final long periodNs;
  private double sum = 0l;

  public SummingComputation(Computation<Double> child, long periodMs) {
    this.child    = child;
    this.periodNs = periodMs * 1_000l * 1_000l;
    addChildren(child);
  }

  @Override
  protected Optional<Double> computeNextResult(GdaxState state, long nanoseconds) {
    if (isSyncing()) { return Optional.empty(); }

    sum += child.getResult();
    history.add(new double[] { nanoseconds, child.getResult() });

    boolean historyComplete = false;
    while (!history.isEmpty()) {
      if ((nanoseconds - history.peek()[0]) > periodNs) {
        historyComplete = true;
        sum -= history.remove()[1];
      } else {
        break;
      }
    }

    if (historyComplete) {
      return Optional.of(sum);
    } else {
      return Optional.empty();
    }
  }

  @Override
  public void onStateSyncStart(long nanoseconds) throws StateProcessingException {
    super.onStateSyncStart(nanoseconds);
    history.clear();
    sum = 0l;
  }

}
