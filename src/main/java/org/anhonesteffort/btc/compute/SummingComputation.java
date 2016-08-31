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
import org.anhonesteffort.btc.state.StateProcessingException;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

public class SummingComputation extends Computation<Optional<Long>> {

  private final Queue<long[]> history = new ArrayDeque<>();
  private final Computation<Long> child;
  private final long periodNs;
  private long sum = 0l;

  public SummingComputation(Computation<Long> child, long periodMs) {
    this.child    = child;
    this.periodNs = periodMs * 1000l * 1000l;
    addChildren(child);
  }

  @Override
  protected Optional<Long> computeNextResult(State state, long nanoseconds) {
    sum += child.getResult();
    history.add(new long[] { nanoseconds, child.getResult() });

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
  public void onStateReset() throws StateProcessingException {
    super.onStateReset();
    history.clear();
    sum = 0l;
  }

}
