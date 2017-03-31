package org.anhonesteffort.trading.strategy

import java.io.IOException

import org.anhonesteffort.trading.http.HttpClientWrapper
import org.anhonesteffort.trading.state.{GdaxState, StateProcessingException}

class OrderCancelingStrategy(http: HttpClientWrapper, orderId: String) extends Strategy[Boolean](false) {

  @volatile private var canceled = false

  try {

    http.cancelOrder(orderId).whenComplete((_, err) => {
      if (err != null) {
        handleAsyncError(new StateProcessingException("cancel order request completed with error", err))
      } else {
        canceled = true
      }
    })

  } catch {
    case e: IOException => handleAsyncError(new StateProcessingException("error encoding api request", e))
  }

  override protected def advanceStrategy(state: GdaxState, ns: Long): Boolean = {
    canceled
  }

}
