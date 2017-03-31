package org.anhonesteffort.trading.strategy

import org.anhonesteffort.trading.book.Orders.Order
import org.anhonesteffort.trading.http.request.RequestFactory
import org.anhonesteffort.trading.http.request.model.PostOrderRequest
import org.anhonesteffort.trading.state.GdaxState

abstract class AskIdentifyingStrategy(requests: RequestFactory) extends Strategy[Option[PostOrderRequest]](None) {

  private var bidPosition : Option[Order] = None
  private var lastAsk     : Option[Order] = None

  def setContext(bidPosition: Option[Order], lastAsk: Option[Order]): Unit = {
    this.bidPosition = bidPosition
    this.lastAsk     = lastAsk
  }

  def identifyPrice(bidPosition: Order, lastAsk: Option[Order], state: GdaxState, ns: Long): Option[Double]

  override protected def advanceStrategy(state: GdaxState, ns: Long): Option[PostOrderRequest] = {
    if (isSyncing || bidPosition.isEmpty) {
      return None
    }

    identifyPrice(bidPosition.get, lastAsk, state, ns) match {
      case None        => None
      case Some(price) => Some(requests.newAsk(price, bidPosition.get.getSize))
    }
  }

}
