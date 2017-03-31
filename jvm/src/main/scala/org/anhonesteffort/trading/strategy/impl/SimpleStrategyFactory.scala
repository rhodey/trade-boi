package org.anhonesteffort.trading.strategy.impl

import org.anhonesteffort.trading.book.Orders.Side
import org.anhonesteffort.trading.http.HttpClientWrapper
import org.anhonesteffort.trading.http.request.RequestFactory
import org.anhonesteffort.trading.strategy.{AskIdentifyingStrategy, BidIdentifyingStrategy, OrderMatchingStrategy, StrategyFactory}

class SimpleStrategyFactory(http: HttpClientWrapper) extends StrategyFactory(http) {

  private val BID_SIZE      : Double = 0.01d
  private val BID_PLACEMENT : Double = 0.75d
  private val BID_ABORT_MS  : Long   = 12000l
  private val ASK_ABORT_MS  : Long   =  2250l

  private val requests : RequestFactory = new RequestFactory()

  override def newBidIdentifying: BidIdentifyingStrategy = {
    new SimpleBidIdentifyingStrategy(requests)
  }

  override def newAskIdentifying: AskIdentifyingStrategy = {
    new SimpleAskIdentifyingStrategy(requests)
  }

  override def newOrderMatching(side: Side, orderId: String): OrderMatchingStrategy = {
    side match {
      case Side.ASK => new SimpleOrderMatchingStrategy(orderId, ASK_ABORT_MS)
      case Side.BID => new SimpleOrderMatchingStrategy(orderId, BID_ABORT_MS)
    }
  }

}
