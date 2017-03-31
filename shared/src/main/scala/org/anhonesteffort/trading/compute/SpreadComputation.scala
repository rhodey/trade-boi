package org.anhonesteffort.trading.compute
import org.anhonesteffort.trading.state.GdaxState

class SpreadComputation extends Computation[Option[Double]](None) {

  override def computeNextResult(state: GdaxState, ns: Long): Option[Double] = {
    (state.orderBook.getAskLimits.peek, state.orderBook.getBidLimits.peek) match {
      case (Some(ask), Some(bid)) => Some(ask.getPrice - bid.getPrice)
      case _ => None
    }
  }

}
