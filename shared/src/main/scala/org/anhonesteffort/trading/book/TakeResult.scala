package org.anhonesteffort.trading.book

import org.anhonesteffort.trading.book.Orders.Order

case class TakeResult(taker: Order, makers: Seq[Order], takeSize: Double) {

  val takeValue : Double = makers.map(_.getValueRemoved).sum

  def clearMakerValueRemoved(): Unit = {
    makers.foreach(_.clearValueRemoved())
  }

}
