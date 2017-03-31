package org.anhonesteffort.trading.book

import org.anhonesteffort.trading.book.Orders.{MarketOrder, Order, Side}

import scala.collection.mutable

class LimitQueue(side: Side) {

  private val map   : mutable.Map[Double, Limit] = new mutable.HashMap[Double, Limit]()
  private val queue : LimitPriorityQueue         = new LimitPriorityQueue(side)

  def peek: Option[Limit] = {
    queue.peek()
  }

  def add(order: Order): Unit = {
    map.get(order.getPrice) match {
      case Some(limit) => limit.add(order)
      case None        =>
        val limit = new Limit(order.getPrice)
        map.put(limit.getPrice, limit)
        queue.enqueue(limit)
        limit.add(order)
    }
  }

  def remove(price: Double, orderId: String): Option[Order] = {
    map.get(price) match {
      case None        => None
      case Some(limit) =>
        val order = limit.remove(orderId)
        if (order.isDefined && limit.peek.isEmpty) {
          map.remove(price)
          queue.dequeue(limit)
        }
        order
    }
  }

  def reduce(price: Double, orderId: String, size: Double): Option[Order] = {
    map.get(price) match {
      case None        => None
      case Some(limit) =>
        val order = limit.reduce(orderId, size)
        if (order.isDefined && limit.peek.isEmpty) {
          map.remove(price)
          queue.dequeue(limit)
        }
        order
    }
  }

  private def isTaken(maker: Limit, taker: Order): Boolean = {
    taker match {
      case marketTaker : MarketOrder => marketTaker.getSizeRemainingFor(maker.getPrice) >= 0d
      case limitTaker  : Order       => limitTaker.getSide match {
        case Side.ASK => limitTaker.getPrice <= maker.getPrice
        case Side.BID => limitTaker.getPrice >= maker.getPrice
      }
    }
  }

  def takeLiquidityFromBestLimit(taker: Order): Seq[Order] = {
    peek match {
      case Some(maker) =>
        if (isTaken(maker, taker)) {
          val makers = maker.takeLiquidity(taker)
          if (makers.nonEmpty && maker.peek.isEmpty) {
            map.remove(maker.getPrice)
            queue.dequeue()
          }
          makers
        } else {
          Seq()
        }

      case None => Seq()
    }
  }

  def clear(): Unit = {
    map.clear()
    while (queue.peek().isDefined) { queue.dequeue().clear() }
  }

}
