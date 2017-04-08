package org.anhonesteffort.trading.compute

import org.anhonesteffort.trading.state.GdaxState

class LatencyComputation() extends Computation[Double](-1l) {

  override def computeNextResult(state: GdaxState, ns: Long): Double = {
    System.nanoTime - ns
  }

}
