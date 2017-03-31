package org.anhonesteffort.trading.state

import org.anhonesteffort.trading.book.LimitOrderBook
import org.anhonesteffort.trading.book.Orders.Order
import org.anhonesteffort.trading.state.Events.OrderEvent

import scala.collection.mutable

case class GdaxState(orderBook: LimitOrderBook) {

  val clientOIdMap   : mutable.Map[String, String] = new mutable.HashMap[String, String]()
  val rxLimitOrders  : mutable.Map[String, Order]  = new mutable.HashMap[String, Order]()
  val marketOrderIds : mutable.Set[String]         = new mutable.HashSet[String]()
  val makers         : mutable.ArrayBuffer[Order]  = new mutable.ArrayBuffer[Order]()
  var event          : Option[OrderEvent]          = None

  def clear(): Unit = {
    orderBook.clear()
    clientOIdMap.clear()
    rxLimitOrders.clear()
    marketOrderIds.clear()
    makers.clear()
    event = None
  }

}
