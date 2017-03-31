package org.anhonesteffort.trading.compute

import org.anhonesteffort.trading.state.GdaxState

class LatencyComputation(mod: Long) extends Computation[Option[Long]](None) {

  private var sequence      = 0l
  private var nanosecondSum = 0l

  override def computeNextResult(state: GdaxState, ns: Long): Option[Long] = {
    sequence += 1

    if ((sequence % mod) == 0l) {
      val copy = nanosecondSum
      nanosecondSum = System.nanoTime - ns
      Some(copy / mod)
    } else {
      nanosecondSum += System.nanoTime - ns
      None
    }
  }

}
