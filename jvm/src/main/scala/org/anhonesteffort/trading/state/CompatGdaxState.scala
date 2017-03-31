package org.anhonesteffort.trading.state

import java.util.Optional

import org.anhonesteffort.trading.book.CompatLimitOrderBook
import org.anhonesteffort.trading.book.Orders.Order
import org.anhonesteffort.trading.state.Events.OrderEvent

import collection.JavaConverters._

class CompatGdaxState(book: CompatLimitOrderBook) extends GdaxState(book) {

  val getOrderBook      : CompatLimitOrderBook          = book
  val getClientOIdMap   : java.util.Map[String, String] = clientOIdMap.asJava
  val getRxLimitOrders  : java.util.Map[String, Order]  = rxLimitOrders.asJava
  val getMarketOrderIds : java.util.Set[String]         = marketOrderIds.asJava
  val getMakers         : java.util.List[Order]         = makers.asJava

  def setEvent(event: OrderEvent): Unit = {
    if (event == null) {
      this.event = None
    } else {
      this.event = Some(event)
    }
  }

  def getEvent: java.util.Optional[OrderEvent] = {
    event match {
      case Some(evt) => Optional.of(evt)
      case None      => Optional.empty()
    }
  }

}
