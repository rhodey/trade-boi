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
import org.anhonesteffort.btc.state.StateListener;
import org.anhonesteffort.btc.state.StateProcessingException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class Computation<T> implements StateListener {

  private final Set<StateListener> children = new HashSet<>();
  protected T result;

  protected Computation() { }
  protected Computation(StateListener ... children) {
    addChildren(children);
  }

  protected void addChildren(StateListener ... children) {
    this.children.addAll(Arrays.asList(children));
  }

  protected void removeChildren(StateListener ... children) {
    this.children.removeAll(Arrays.asList(children));
  }

  protected abstract T computeNextResult(State state, long nanoseconds) throws StateProcessingException;

  public T getResult() {
    return result;
  }

  @Override
  public void onStateChange(State state, long nanoseconds) throws StateProcessingException {
    for (StateListener child : children) { child.onStateChange(state, nanoseconds); }
    result = computeNextResult(state, nanoseconds);
  }

  @Override
  public void onStateReset() throws StateProcessingException {
    for (StateListener child : children) { child.onStateReset(); }
  }

}
