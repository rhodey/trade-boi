package org.anhonesteffort.trading.book

import org.anhonesteffort.trading.book.Orders.{MarketOrder, Order}

import scala.collection.mutable

class Limit(price: Double) {

  private val map   : mutable.Map[String, Order] = new mutable.HashMap[String, Order]()
  private val queue : mutable.Queue[Order]       = new mutable.Queue[Order]()

  private var volume : Double = 0d

  def getPrice  : Double = price
  def getVolume : Double = volume

  def peek: Option[Order] = {
    queue.headOption
  }

  def add(order: Order): Unit = {
    map.put(order.getOrderId, order)
    queue.enqueue(order)
    volume += order.getSizeRemaining
  }

  def remove(orderId: String): Option[Order] = {
    map.remove(orderId) match {
      case None        => None
      case Some(order) =>
        queue.dequeueAll(_.equals(order))
        volume -= order.getSizeRemaining
        Some(order)
    }
  }

  def reduce(orderId: String, size: Double): Option[Order] = {
    map.get(orderId) match {
      case None        => None
      case Some(order) =>
        order.subtract(size, price)
        volume -= size
        if (order.getSizeRemaining <= 0d) {
          map.remove(orderId)
          queue.dequeueAll(_.equals(order))
        }
        Some(order)
    }
  }

  private def getTakeSize(taker: Order): Double = {
    taker match {
      case market : MarketOrder => market.getSizeRemainingFor(price)
      case limit  : Order       => limit.getSizeRemaining
    }
  }

  private def takeLiquidityFromNextMaker(taker: Order, takeSize: Double): Option[Order] = {
    queue.headOption match {
      case None        => None
      case Some(maker) =>
        val volumeRemoved = maker.takeSize(takeSize)

        if (maker.getSizeRemaining <= 0d) {
          map.remove(maker.getOrderId)
          queue.dequeue()
        }

        volume -= volumeRemoved
        taker.subtract(volumeRemoved, price)
        Some(maker)
    }
  }

  def takeLiquidity(taker: Order): Seq[Order] = {
    val makers   : mutable.Buffer[Order] = new mutable.ArrayBuffer[Order]()
    var takeSize : Double                = getTakeSize(taker)

    while (takeSize > 0d) {
      takeLiquidityFromNextMaker(taker, takeSize) match {
        case None        => takeSize = -1d;
        case Some(maker) =>
          makers  += maker
          takeSize = getTakeSize(taker)
      }
    }

    makers
  }

  def clear(): Unit = {
    queue.clear()
    map.clear()
    volume = 0d
  }

}
