package org.anhonesteffort.trading.strategy

import org.anhonesteffort.trading.state.Events.Type
import org.anhonesteffort.trading.state.{GdaxState, StateProcessingException}
import org.slf4j.{Logger, LoggerFactory}

abstract class OrderMatchingStrategy(orderId: String) extends AbortableStrategy[Boolean](false) {

  private val log: Logger = LoggerFactory.getLogger(classOf[OrderMatchingStrategy])

  protected def shouldAbort(state: GdaxState, ns: Long): Boolean

  override protected def advanceStrategy(state: GdaxState, ns: Long): Boolean = {
    if (isSyncing) {
      throw new StateProcessingException("unable to handle state synchronization")
    } else if (shouldAbort(state, ns)) {
      super.abort()
      return false
    }

    state.event match {
      case None        => Unit
      case Some(event) =>
        if (event.typee != Type.OPEN && event.orderId == orderId) {
          throw new StateProcessingException("order took, reduced, or canceled unexpectedly")
        }
    }

    state.makers.find(_.getOrderId == orderId) match {
      case None        => false
      case Some(maker) =>
        if (maker.getSizeRemaining > 0d) {
          log.info("order partially matched, " + maker.getSizeRemaining + " remaining")
          false
        } else {
          true
        }
    }
  }

}
