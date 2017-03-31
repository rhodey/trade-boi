package org.anhonesteffort.trading.book

import org.anhonesteffort.trading.book.Orders.Order

import collection.JavaConverters._

class CompatTakeResult(delegate: TakeResult) {

  val getTaker     : Order                 = delegate.taker
  val getMakers    : java.util.List[Order] = delegate.makers.asJava
  val getTakeSize  : Double                = delegate.takeSize
  val getTakeValue : Double                = delegate.takeValue

}
