package org.anhonesteffort.trading.state

trait StateListener {

  @throws[StateProcessingException]
  def onStateChange(state: GdaxState, ns: Long)

  @throws[StateProcessingException]
  def onStateSyncStart(ns: Long)

  @throws[StateProcessingException]
  def onStateSyncEnd(ns: Long)

}
