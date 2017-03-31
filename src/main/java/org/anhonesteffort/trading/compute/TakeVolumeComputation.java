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
import org.anhonesteffort.trading.book.Order;
import org.anhonesteffort.trading.proto.OrderEvent;

public class TakeVolumeComputation extends Computation<Double> {

  private final Order.Side side;

  public TakeVolumeComputation(Order.Side side) {
    this.side = side;
  }

  @Override
  protected Double computeNextResult(GdaxState state, long nanoseconds) {
    if (!state.getEvent().isPresent()) {
      return 0d;
    } else if (!state.getEvent().get().getType().equals(OrderEvent.Type.TAKE)) {
      return 0d;
    } else if (!state.getEvent().get().getSide().equals(side)) {
      return 0d;
    } else {
      return state.getEvent().get().getSize();
    }
  }

}
