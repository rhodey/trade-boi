package org.anhonesteffort.trading.book

import org.anhonesteffort.trading.book.Orders.{MarketOrder, Order, Side}

import scala.collection.mutable

class LimitOrderBook {

  private val askLimits = new LimitQueue(Side.ASK)
  private val bidLimits = new LimitQueue(Side.BID)

  def getAskLimits: LimitQueue = {
    askLimits
  }

  def getBidLimits: LimitQueue = {
    bidLimits
  }

  private def processAsk(ask: Order): Seq[Order] = {
    val makers = new mutable.ArrayBuffer[Order]()
    var next   = bidLimits.takeLiquidityFromBestLimit(ask)

    while (next.nonEmpty) {
      makers ++= next
      next = bidLimits.takeLiquidityFromBestLimit(ask)
    }

    ask match {
      case _     : MarketOrder => makers
      case limit : Order       =>
        if (limit.getSizeRemaining > 0d) {
          askLimits.add(ask)
        }
        makers
    }
  }

  private def processBid(bid: Order): Seq[Order] = {
    val makers = new mutable.ArrayBuffer[Order]()
    var next   = askLimits.takeLiquidityFromBestLimit(bid)

    while (next.nonEmpty) {
      makers ++= next
      next = askLimits.takeLiquidityFromBestLimit(bid)
    }

    bid match {
      case _     : MarketOrder => makers
      case limit : Order       =>
        if (limit.getSizeRemaining > 0d) {
          bidLimits.add(bid)
        }
        makers
    }
  }

  private def resultFor(taker: Order, makers: Seq[Order], priorSize: Double): TakeResult = {
    taker match {
      case market : MarketOrder => TakeResult(market, makers, market.getVolumeRemoved)
      case limit  : Order       => TakeResult(limit, makers, priorSize - limit.getSizeRemaining)
    }
  }

  def add(taker: Order): TakeResult = {
    val priorSize = taker.getSizeRemaining
    val makers    = taker.getSide match {
      case Side.ASK => processAsk(taker)
      case Side.BID => processBid(taker)
    }

    resultFor(taker, makers, priorSize)
  }

  def remove(side: Side, price: Double, orderId: String): Option[Order] = {
    side match {
      case Side.ASK => askLimits.remove(price, orderId)
      case Side.BID => bidLimits.remove(price, orderId)
    }
  }

  def reduce(side: Side, price: Double, orderId: String, size: Double): Option[Order] = {
    side match {
      case Side.ASK => askLimits.reduce(price, orderId, size)
      case Side.BID => bidLimits.reduce(price, orderId, size)
    }
  }

  def clear(): Unit = {
    askLimits.clear()
    bidLimits.clear()
  }

}
