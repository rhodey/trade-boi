package org.anhonesteffort.trading.compute

import org.anhonesteffort.trading.state.{GdaxState, StateListener, StateProcessingException}

import scala.collection.mutable

abstract class Computation[T](initial: T) extends StateListener {

  private val children : mutable.Set[StateListener] = new mutable.HashSet[StateListener]()

  private var syncing : Boolean = false
  private var result  : T       = initial

  protected def addChild(child: StateListener): Unit = {
    this.children += child
  }

  protected def addChildren(children: Seq[StateListener]): Unit = {
    this.children ++= children
  }

  protected def removeChild(child: StateListener): Unit = {
    this.children -= child
  }

  protected def removeChildren(children: Seq[StateListener]): Unit = {
    this.children --= children
  }

  protected def isSyncing: Boolean = syncing

  @throws[StateProcessingException]
  protected def computeNextResult(state: GdaxState, ns: Long): T

  def getResult: T = result

  override def onStateChange(state: GdaxState, ns: Long): Unit = {
    children.foreach(_.onStateChange(state, ns))
    result = computeNextResult(state, ns)
  }

  override def onStateSyncStart(ns: Long): Unit = {
    syncing = true
    children.foreach(_.onStateSyncStart(ns))
  }

  override def onStateSyncEnd(ns: Long): Unit = {
    syncing = false
    children.foreach(_.onStateSyncEnd(ns))
  }

}
