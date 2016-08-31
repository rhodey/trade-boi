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

import org.anhonesteffort.btc.state.CriticalStateProcessingException;
import org.anhonesteffort.btc.state.State;
import org.anhonesteffort.btc.state.StateProcessingException;

public class BidMatchingStrategy extends Strategy<Boolean> {

  private final String orderId;

  public BidMatchingStrategy(String orderId) {
    this.orderId = orderId;
  }

  @Override
  protected Boolean advanceStrategy(State state, long nanoseconds) {
    return Boolean.FALSE;
  }

  @Override
  public void onStateReset() throws StateProcessingException {
    throw new CriticalStateProcessingException("unable to handle state reset");
  }

}
