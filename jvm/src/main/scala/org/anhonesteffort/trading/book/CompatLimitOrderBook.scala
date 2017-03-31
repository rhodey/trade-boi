package org.anhonesteffort.trading.book

import java.util.Optional

import org.anhonesteffort.trading.book.Orders.{Order, Side}

class CompatLimitOrderBook extends LimitOrderBook {

  def jadd(taker: Order): CompatTakeResult = {
    new CompatTakeResult(super.add(taker))
  }

  def jremove(side: Side, price: Double, orderId: String): java.util.Optional[Order] = {
    super.remove(side, price, orderId) match {
      case Some(order) => Optional.of(order)
      case None        => Optional.empty()
    }
  }

  def jreduce(side: Side, price: Double, orderId: String, size: Double): java.util.Optional[Order] = {
    super.reduce(side, price, orderId, size) match {
      case Some(order) => Optional.of(order)
      case None        => Optional.empty()
    }
  }

}
