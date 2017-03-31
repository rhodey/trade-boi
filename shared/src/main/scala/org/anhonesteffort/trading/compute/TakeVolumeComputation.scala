package org.anhonesteffort.trading.compute

import org.anhonesteffort.trading.book.Orders.Side
import org.anhonesteffort.trading.state.Events.Type
import org.anhonesteffort.trading.state.GdaxState

class TakeVolumeComputation(side: Side) extends Computation[Double](0d) {

  override def computeNextResult(state: GdaxState, ns: Long): Double = {
    if (state.event.isEmpty) {
      0d
    } else if (state.event.get.typee != Type.TAKE || state.event.get.side != side) {
      0d
    } else {
      state.event.get.size
    }
  }

}
