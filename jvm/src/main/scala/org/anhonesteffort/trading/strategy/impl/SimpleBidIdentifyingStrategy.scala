package org.anhonesteffort.trading.strategy.impl

import org.anhonesteffort.trading.http.request.RequestFactory
import org.anhonesteffort.trading.http.request.model.PostOrderRequest
import org.anhonesteffort.trading.state.GdaxState
import org.anhonesteffort.trading.strategy.BidIdentifyingStrategy

class SimpleBidIdentifyingStrategy(requests: RequestFactory) extends BidIdentifyingStrategy(requests) {

  override protected def identifyBid(state: GdaxState, ns: Long): Option[PostOrderRequest] = {
    None
  }

}
