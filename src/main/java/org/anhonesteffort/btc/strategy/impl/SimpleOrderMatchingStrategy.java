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

import org.anhonesteffort.btc.state.GdaxState;
import org.anhonesteffort.btc.strategy.OrderMatchingStrategy;

public class SimpleOrderMatchingStrategy extends OrderMatchingStrategy {

  private final long abortNs;
  private long startNs;

  public SimpleOrderMatchingStrategy(String orderId, long abortMs) {
    super(orderId);
    this.abortNs = abortMs * 1_000_000l;
    startNs      = -1l;
  }

  @Override
  protected boolean shouldAbort(GdaxState state, long nanoseconds) {
    if (startNs == -1l) {
      startNs = nanoseconds;
      return false;
    } else {
      return (nanoseconds - startNs) >= abortNs;
    }
  }

}
