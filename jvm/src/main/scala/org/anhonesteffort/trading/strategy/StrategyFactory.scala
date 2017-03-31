package org.anhonesteffort.trading.strategy

import org.anhonesteffort.trading.book.Orders
import org.anhonesteffort.trading.http.HttpClientWrapper
import org.anhonesteffort.trading.http.request.model.PostOrderRequest

abstract class StrategyFactory(http: HttpClientWrapper) {

  def newBidIdentifying: BidIdentifyingStrategy

  def newAskIdentifying: AskIdentifyingStrategy

  def newOrderMatching(side: Orders.Side, orderId: String): OrderMatchingStrategy

  def newOrderOpening(order: PostOrderRequest): OrderOpeningStrategy = {
    new OrderOpeningStrategy(http, order)
  }

  def newOrderCanceling(orderId: String): OrderCancelingStrategy = {
    new OrderCancelingStrategy(http, orderId)
  }

}
