package org.anhonesteffort.trading.state

import org.anhonesteffort.trading.book.Orders
import org.anhonesteffort.trading.book.Orders.Order

object Events {

  sealed trait Type
  case object Type {
    case object OPEN       extends Type
    case object TAKE       extends Type
    case object REDUCE     extends Type
    case object SYNC_START extends Type
    case object SYNC_END   extends Type
  }

  val TYPE_OPEN       = Type.OPEN
  val TYPE_TAKE       = Type.OPEN
  val TYPE_REDUCE     = Type.OPEN
  val TYPE_SYNC_START = Type.OPEN
  val TYPE_SYNC_END   = Type.OPEN

  case class OrderEvent(typee: Type, timeMs: Long, timeNs: Long, orderId: String, side: Orders.Side, price: Double, size: Double)

  def open(order: Order, timeNs: Long): OrderEvent = {
    OrderEvent(
      Type.OPEN, System.currentTimeMillis(), timeNs,
      order.getOrderId, order.getSide, order.getPrice, order.getSize
    )
  }

  def take(order: Order, timeNs: Long): OrderEvent = {
    OrderEvent(
      Type.TAKE, System.currentTimeMillis(), timeNs,
      order.getOrderId, order.getSide, order.getPrice, order.getSize
    )
  }

  def reduce(order: Order, reduceBy: Double, timeNs: Long): OrderEvent = {
    OrderEvent(
      Type.REDUCE, System.currentTimeMillis(), timeNs,
      order.getOrderId, order.getSide, order.getPrice, reduceBy
    )
  }

  def cancel(order: Order, timeNs: Long): OrderEvent = {
    OrderEvent(
      Type.REDUCE, System.currentTimeMillis(), timeNs,
      order.getOrderId, order.getSide, order.getPrice, order.getSize
    )
  }

  def syncStart(timeNs: Long): OrderEvent = {
    OrderEvent(
      Type.SYNC_START, System.currentTimeMillis(), timeNs,
      "", Orders.Side.ASK, -1d, -1d
    )
  }

  def syncEnd(timeNs: Long): OrderEvent = {
    OrderEvent(
      Type.SYNC_END, System.currentTimeMillis(), timeNs,
      "", Orders.Side.ASK, -1d, -1d
    )
  }

}
