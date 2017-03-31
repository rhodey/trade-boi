package org.anhonesteffort.trading.strategy

import java.io.IOException

import org.anhonesteffort.trading.book.Orders.{Order, Side}
import org.anhonesteffort.trading.http.HttpClientWrapper
import org.anhonesteffort.trading.http.request.model.PostOrderRequest
import org.anhonesteffort.trading.state.{GdaxState, StateProcessingException}

class OrderOpeningStrategy(http: HttpClientWrapper, postOrder: PostOrderRequest) extends AbortableStrategy[Option[Order]](None) {

  try {

    http.postOrder(postOrder).whenComplete((ok, err) => {
      if (err != null) {
        handleAsyncError(new StateProcessingException("post order request completed with error", err))
      } else if (!ok) {
        super.abort()
      }
    })

  } catch {
    case e: IOException => handleAsyncError(new StateProcessingException("error encoding api request", e))
  }

  private def sideMatches(postOrder: PostOrderRequest, bookOrder: Order): Boolean = {
    bookOrder.getSide match {
      case Side.ASK => postOrder.getSide == "sell"
      case Side.BID => postOrder.getSide == "buy"
    }
  }

  override protected def advanceStrategy(state: GdaxState, ns: Long): Option[Order] = {
    val bookOid   = state.clientOIdMap.get(postOrder.getClientOid)
    val bookOrder = bookOid match {
      case None      => None
      case Some(oid) => state.rxLimitOrders.get(oid)
    }

    if (isSyncing) {
      throw new StateProcessingException("unable to handle state synchronization")
    } else if (bookOid.isEmpty) {
      None
    } else if (bookOrder.isEmpty) {
      throw new StateProcessingException("order id map entry not found in rx limit order map")
    } else if (!sideMatches(postOrder, bookOrder.get)) {
      throw new StateProcessingException("posted order ended up on wrong side of the book")
    } else {
      bookOrder
    }
  }

}
