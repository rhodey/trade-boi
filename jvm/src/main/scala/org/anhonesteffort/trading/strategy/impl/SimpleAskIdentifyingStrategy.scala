package org.anhonesteffort.trading.strategy.impl

import org.anhonesteffort.trading.book.Orders.Order
import org.anhonesteffort.trading.http.request.RequestFactory
import org.anhonesteffort.trading.state.GdaxState
import org.anhonesteffort.trading.strategy.AskIdentifyingStrategy

class SimpleAskIdentifyingStrategy(requests: RequestFactory) extends AskIdentifyingStrategy(requests) {

  override def identifyPrice(bidPosition: Order, lastAsk: Option[Order], state: GdaxState, ns: Long): Option[Double] = {
    val bidFloor   : Double = state.orderBook.getBidLimits.peek.get.getPrice
    val askCeiling : Double = state.orderBook.getAskLimits.peek.get.getPrice
    val lastPrice  : Double = lastAsk match { case None => -1d case Some(price) => price.getPrice}
    val nextPrice  : Double = lastPrice - 0.01d
    val bidPrice   : Double = bidPosition.getPrice

    if (lastAsk.isEmpty) {
      Some(askCeiling)
    } else if (nextPrice > bidFloor && nextPrice > bidPrice) {
      Some(nextPrice)
    } else if (bidPrice > bidFloor) {
      Some(bidPrice)
    } else {
      Some(bidFloor + 0.01d)
    }
  }

}
