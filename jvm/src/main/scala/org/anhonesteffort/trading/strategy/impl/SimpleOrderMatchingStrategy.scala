package org.anhonesteffort.trading.strategy.impl

import org.anhonesteffort.trading.state.GdaxState
import org.anhonesteffort.trading.strategy.OrderMatchingStrategy

class SimpleOrderMatchingStrategy(orderId: String, abortMs: Long) extends OrderMatchingStrategy(orderId) {

  private val abortNs : Long = abortMs * 1000l * 1000l
  private var startNs : Long = -1l

  override protected def shouldAbort(state: GdaxState, ns: Long): Boolean = {
    if (startNs == -1l) {
      startNs = ns
      false
    } else {
      (ns - startNs) >= abortNs
    }
  }

}
