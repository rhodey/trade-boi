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

package org.anhonesteffort.trading.strategy;

import org.anhonesteffort.trading.compute.Computation;
import org.anhonesteffort.trading.state.GdaxState;
import org.anhonesteffort.trading.state.StateProcessingException;

import java.util.concurrent.atomic.AtomicReference;

public abstract class Strategy<T> extends Computation<T> {

  private final AtomicReference<StateProcessingException> error = new AtomicReference<>(null);

  protected void handleAsyncError(StateProcessingException err) {
    error.set(err);
  }

  protected abstract T advanceStrategy(GdaxState state, long nanoseconds) throws StateProcessingException;

  @Override
  protected T computeNextResult(GdaxState state, long nanoseconds) throws StateProcessingException {
    if (error.get() != null) {
      throw error.getAndSet(null);
    } else {
      return advanceStrategy(state, nanoseconds);
    }
  }

  @Override
  public void onStateSyncStart(long nanoseconds) throws StateProcessingException {
    super.onStateSyncStart(nanoseconds);
    if (error.get() != null) { throw error.getAndSet(null); }
  }

  @Override
  public void onStateSyncEnd(long nanoseconds) throws StateProcessingException {
    super.onStateSyncEnd(nanoseconds);
    if (error.get() != null) { throw error.getAndSet(null); }
  }

}
