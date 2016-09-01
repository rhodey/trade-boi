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

import org.anhonesteffort.btc.compute.Computation;
import org.anhonesteffort.btc.state.State;
import org.anhonesteffort.btc.state.StateProcessingException;

import java.util.Optional;

public abstract class Strategy<T> extends Computation<T> {

  private boolean abort = false;
  private Optional<StateProcessingException> error = Optional.empty();

  protected boolean isAborted() { return abort; }
  protected void abort() { abort = true; }

  protected void handleAsyncError(StateProcessingException error) {
    this.error = Optional.of(error);
  }

  protected abstract T advanceStrategy(State state, long nanoseconds) throws StateProcessingException;

  @Override
  protected T computeNextResult(State state, long nanoseconds) throws StateProcessingException {
    if (error.isPresent()) {
      throw error.get();
    } else {
      return advanceStrategy(state, nanoseconds);
    }
  }

  @Override
  public void onStateReset() throws StateProcessingException {
    super.onStateReset();
    if (error.isPresent()) { throw error.get(); }
  }

}
