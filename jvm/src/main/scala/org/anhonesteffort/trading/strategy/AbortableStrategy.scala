package org.anhonesteffort.trading.strategy

abstract class AbortableStrategy[T](initial: T) extends Strategy[T](initial) {

  @volatile private var abortt : Boolean = false

  def isAborted: Boolean = abortt

  protected def abort(): Unit = { abortt = true }

}
