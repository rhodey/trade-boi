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

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

public class SummingComputation extends Computation<Optional<Long>> {

  private final Queue<ChildResult> history = new ArrayDeque<>();
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
    history.add(new ChildResult(child.getResult(), nanoseconds));

    boolean historyComplete = false;
    while (!history.isEmpty()) {
      if ((nanoseconds - history.peek().nanoseconds) > periodNs) {
        historyComplete = true;
        sum -= history.remove().result;
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
  public void onStateReset() {
    history.clear();
    sum = 0l;
    super.onStateReset();
  }

  private static class ChildResult {
    private final long result;
    private final long nanoseconds;
    public ChildResult(long result, long nanoseconds) {
      this.result      = result;
      this.nanoseconds = nanoseconds;
    }
  }

}
