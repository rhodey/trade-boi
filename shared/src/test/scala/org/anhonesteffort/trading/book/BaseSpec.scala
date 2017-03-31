package org.anhonesteffort.trading.book

import org.anhonesteffort.trading.book.Orders.{MarketOrder, Order, Side}
import org.scalatest.FlatSpec

class BaseSpec extends FlatSpec {

  private var nextOrderId = 0

  protected def newAsk(orderId: String, price: Double, size: Double): Order = {
    new Order(orderId, Side.ASK, price, size)
  }

  protected def newAsk(price: Double, size: Double): Order = {
    nextOrderId += 1
    this.newAsk(nextOrderId.toString, price, size)
  }

  protected def newMarketAsk(orderId: String, size: Double, funds: Double): MarketOrder = {
    new MarketOrder(orderId, Side.ASK, size, funds)
  }

  protected def newMarketAsk(size: Double, funds: Double): MarketOrder = {
    nextOrderId += 1
    this.newMarketAsk(nextOrderId.toString, size, funds)
  }

  protected def newBid(orderId: String, price: Double, size: Double): Order = {
    new Order(orderId, Side.BID, price, size)
  }

  protected def newBid(price: Double, size: Double): Order = {
    nextOrderId += 1
    this.newBid(nextOrderId.toString, price, size)
  }

  protected def newMarketBid(orderId: String, size: Double, funds: Double): MarketOrder = {
    new MarketOrder(orderId, Side.BID, size, funds)
  }

  protected def newMarketBid(size: Double, funds: Double): MarketOrder = {
    nextOrderId += 1
    this.newMarketBid(nextOrderId.toString, size, funds)
  }

}
