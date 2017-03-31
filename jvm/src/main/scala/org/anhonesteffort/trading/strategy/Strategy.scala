package org.anhonesteffort.trading.strategy

import org.anhonesteffort.trading.compute.Computation
import org.anhonesteffort.trading.state.{GdaxState, StateProcessingException}

abstract class Strategy[T](initial: T) extends Computation[T](initial) {

  @volatile private var error : StateProcessingException = _

  protected def handleAsyncError(err : StateProcessingException): Unit = {error = err}

  @throws[StateProcessingException]
  protected def advanceStrategy(state: GdaxState, ns : Long): T

  override protected def computeNextResult(state: GdaxState, ns: Long): T = {
    if (error != null) {
      throw error
    } else {
      advanceStrategy(state, ns)
    }
  }

  override def onStateSyncStart(ns: Long) {
    super.onStateSyncStart(ns)
    if (error != null) throw error
  }

  override def onStateSyncEnd(ns: Long) {
    super.onStateSyncEnd(ns)
    if (error != null) throw error
  }

}
