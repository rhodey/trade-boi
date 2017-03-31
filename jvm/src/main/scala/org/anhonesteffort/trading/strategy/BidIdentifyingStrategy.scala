package org.anhonesteffort.trading.strategy

import org.anhonesteffort.trading.http.request.RequestFactory
import org.anhonesteffort.trading.http.request.model.PostOrderRequest
import org.anhonesteffort.trading.state.GdaxState

abstract class BidIdentifyingStrategy(requests: RequestFactory) extends Strategy[Option[PostOrderRequest]](None) {

  protected def bidRequest(price: Double, size: Double): PostOrderRequest = {
    requests.newBid(price, size)
  }

  protected def identifyBid(state: GdaxState, ns: Long): Option[PostOrderRequest]

  override protected def advanceStrategy(state: GdaxState, ns: Long): Option[PostOrderRequest] = {
    if (isSyncing) {
      None
    } else {
      identifyBid(state, ns)
    }
  }

}
