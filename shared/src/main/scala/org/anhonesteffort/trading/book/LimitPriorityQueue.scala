package org.anhonesteffort.trading.book

import java.util.Comparator

import org.anhonesteffort.trading.book.Orders.Side

protected class LimitPriorityQueue(side: Side) {

  private val queue = side match {
    case Side.ASK => new java.util.PriorityQueue[Limit](new AskSorter)
    case Side.BID => new java.util.PriorityQueue[Limit](new BidSorter)
  }

  def peek(): Option[Limit] = {
    Option(queue.peek())
  }

  def enqueue(limit: Limit): Unit = {
    queue.add(limit)
  }

  def dequeue(): Limit = {
    queue.remove()
  }

  def dequeue(limit: Limit): Boolean = {
    queue.remove(limit)
  }

  private class AskSorter extends Comparator[Limit] {
    def compare(ask1: Limit, ask2: Limit): Int = {
      if (ask1.getPrice < ask2.getPrice) {
        -1
      } else if (ask1.getPrice == ask2.getPrice) {
        0
      } else {
        1
      }
    }
  }

  private class BidSorter extends Comparator[Limit] {
    def compare(bid1: Limit, bid2: Limit): Int = {
      if (bid1.getPrice > bid2.getPrice) {
        -1
      } else if (bid1.getPrice == bid2.getPrice) {
        0
      } else {
        1
      }
    }
  }

}
