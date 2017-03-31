package org.anhonesteffort.trading.book

import org.anhonesteffort.trading.book.Orders.Side

class LimitOrderBookSpec extends BaseSpec {

  "add, remove, clear ask" should "work" in {
    val BOOK = new LimitOrderBook()

    BOOK.add(newAsk("00", 10, 20))
    BOOK.add(newAsk("01", 30, 40))

    assert(BOOK.remove(Side.ASK, 10d, "00").isDefined)
    BOOK.clear()
    assert(BOOK.remove(Side.ASK, 30d, "01").isEmpty)
  }

  "add, remove, clear bid" should "work" in {
    val BOOK = new LimitOrderBook()

    BOOK.add(newBid("00", 10, 20))
    BOOK.add(newBid("01", 30, 40))

    assert(BOOK.remove(Side.BID, 10d, "00").isDefined)
    BOOK.clear()
    assert(BOOK.remove(Side.BID, 30d, "01").isEmpty)
  }

  "ask" should "not take empty book" in {
    val BOOK   = new LimitOrderBook()
    val RESULT = BOOK.add(newAsk(10, 10))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)
  }

  "bid" should "not take empty book" in {
    val BOOK   = new LimitOrderBook()
    val RESULT = BOOK.add(newBid(10, 10))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)
  }

  "market ask" should "not take empty book" in {
    val BOOK   = new LimitOrderBook()
    val TAKER  = newMarketAsk("00", 10, 20)
    val RESULT = BOOK.add(TAKER)

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    assert(BOOK.remove(Side.ASK, 10d, "00").isEmpty)
    assert(BOOK.remove(Side.ASK, 20d, "00").isEmpty)
  }

  "market bid" should "not take empty book" in {
    val BOOK   = new LimitOrderBook()
    val TAKER  = newMarketBid("00", 10, 20)
    val RESULT = BOOK.add(TAKER)

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    assert(BOOK.remove(Side.BID, 10d, "00").isEmpty)
    assert(BOOK.remove(Side.BID, 20d, "00").isEmpty)
  }

  "ask" should "not take smaller bid" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newBid(8, 10))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newAsk(9, 10))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)
  }

  "bid" should "not take larger ask" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newAsk(8, 10))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newBid(7, 10))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)
  }

  "one ask" should "take one smaller size bid" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newBid(10, 5))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newAsk(10, 20))
    RESULT.takeSize    === 5
    RESULT.takeValue   === 10 * 5
    RESULT.makers.size === 1
  }

  "one ask" should "take one equal size bid" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newBid(10, 5))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newAsk(10, 5))
    RESULT.takeSize    === 5
    RESULT.takeValue   === 10 * 5
    RESULT.makers.size === 1
  }

  "one ask" should "take one larger size bid" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newBid(10, 15))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newAsk(10, 5))
    RESULT.takeSize    === 5
    RESULT.takeValue   === 10 * 5
    RESULT.makers.size === 1
  }

  "one bid" should "take one smaller size ask" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newAsk(10, 5))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newBid(10, 8))
    RESULT.takeSize    === 5
    RESULT.takeValue   === 10 * 5
    RESULT.makers.size === 1
  }

  "one bid" should "take one equal size ask" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newAsk(10, 5))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newBid(10, 5))
    RESULT.takeSize    === 5
    RESULT.takeValue   === 10 * 5
    RESULT.makers.size === 1
  }

  "one bid" should "take one larger size ask" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newAsk(10, 5))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newBid(10, 2))
    RESULT.takeSize    === 2
    RESULT.takeValue   === 10 * 2
    RESULT.makers.size === 1
  }

  "one market size ask" should "take one smaller size bid" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newBid(10, 5))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newMarketAsk(10, -1))
    RESULT.takeSize    === 5
    RESULT.takeValue   === 10 * 5
    RESULT.makers.size === 1
  }

  "one market size ask" should "take one equal size bid" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newBid(10, 5))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newMarketAsk(5, -1))
    RESULT.takeSize    === 5
    RESULT.takeValue   === 10 * 5
    RESULT.makers.size === 1
  }

  "one market size ask" should "take one larger size bid" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newBid(10, 8))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newMarketAsk(5, -1))
    RESULT.takeSize    === 5
    RESULT.takeValue   === 10 * 5
    RESULT.makers.size === 1
  }

  "one market funds ask" should "take one smaller size bid" in {
    val BOOK   = new LimitOrderBook()
    var     RESULT = BOOK.add(newBid(1, 5))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newMarketAsk(-1, 10))
    RESULT.takeSize    === 5
    RESULT.takeValue   === 5
    RESULT.makers.size === 1
  }

  "one market funds ask" should "take one equal size bid" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newBid(1, 5))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newMarketAsk(-1, 5))
    RESULT.takeSize    === 5
    RESULT.takeValue   === 5
    RESULT.makers.size === 1
  }

  "one market funds ask" should "take one larger size bid" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newBid(1, 10))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newMarketAsk(-1, 5))
    RESULT.takeSize    === 5
    RESULT.takeValue   === 5
    RESULT.makers.size === 1
  }

  "one market size bid" should "take one smaller size ask" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newAsk(10, 5))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newMarketBid(10, -1))
    RESULT.takeSize    === 5
    RESULT.takeValue   === 10 * 5
    RESULT.makers.size === 1
  }

  "one market size bid" should "take one equal size ask" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newAsk(10, 10))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newMarketBid(10, -1))
    RESULT.takeSize    === 10
    RESULT.takeValue   === 10 * 10
    RESULT.makers.size === 1
  }

  "one market size bid" should "take one larger size ask" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newAsk(10, 10))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newMarketBid(5, -1))
    RESULT.takeSize    === 5
    RESULT.takeValue   === 10 * 5
    RESULT.makers.size === 1
  }

  "two asks" should "take one smaller size bid" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newBid(10, 20))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newAsk(10, 5))
    RESULT.takeSize    === 5
    RESULT.takeValue   === 10 * 5
    RESULT.makers.size === 1
    RESULT.clearMakerValueRemoved()

    RESULT = BOOK.add(newAsk(10, 25))
    RESULT.takeSize    === 15
    RESULT.takeValue   === 10 * 15
    RESULT.makers.size === 1
  }

  "two asks" should "take one equal size bid" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newBid(10, 20))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newAsk(10, 12))
    RESULT.takeSize    === 12
    RESULT.takeValue   === 10 * 12
    RESULT.makers.size === 1
    RESULT.clearMakerValueRemoved()

    RESULT = BOOK.add(newAsk(10, 8))
    RESULT.takeSize    === 8
    RESULT.takeValue   === 10 * 8
    RESULT.makers.size === 1
  }

  "two asks" should "take one larger size bid" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newBid(10, 30))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newAsk(10, 12))
    RESULT.takeSize    === 12
    RESULT.takeValue   === 10 * 12
    RESULT.makers.size === 1
    RESULT.clearMakerValueRemoved()

    RESULT = BOOK.add(newAsk(10, 8))
    RESULT.takeSize    === 8
    RESULT.takeValue   === 10 * 8
    RESULT.makers.size === 1
  }

  "two bids" should "take one smaller size ask" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newAsk(10, 20))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newBid(10, 5))
    RESULT.takeSize    === 5
    RESULT.takeValue   === 10 * 5
    RESULT.makers.size === 1
    RESULT.clearMakerValueRemoved()

    RESULT = BOOK.add(newBid(10, 25))
    RESULT.takeSize    === 15
    RESULT.takeValue   === 10 * 15
    RESULT.makers.size === 1
  }

  "two bids" should "take one equal size ask" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newAsk(10, 20))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newBid(10, 9))
    RESULT.takeSize    === 9
    RESULT.takeValue   === 10 * 9
    RESULT.makers.size === 1
    RESULT.clearMakerValueRemoved()

    RESULT = BOOK.add(newBid(10, 11))
    RESULT.takeSize    === 11
    RESULT.takeValue   === 10 * 11
    RESULT.makers.size === 1
  }

  "two bids" should "take one larger size ask" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newAsk(10, 30))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newBid(10, 5))
    RESULT.takeSize    === 5
    RESULT.takeValue   === 10 * 5
    RESULT.makers.size === 1
    RESULT.clearMakerValueRemoved()

    RESULT = BOOK.add(newBid(10, 6))
    RESULT.takeSize    === 6
    RESULT.takeValue   === 10 * 6
    RESULT.makers.size === 1
  }

  "two market asks" should "take one smaller size bid" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newBid(20, 15))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newMarketAsk(10, -1))
    RESULT.takeSize    === 10
    RESULT.takeValue   === 10 * 20
    RESULT.makers.size === 1
    RESULT.clearMakerValueRemoved()

    RESULT = BOOK.add(newMarketAsk(10, -1))
    RESULT.takeSize    === 5
    RESULT.takeValue   === 5 * 20
    RESULT.makers.size === 1
  }

  "one ask" should "take two smaller size bids" in {
    val BOOK   = new LimitOrderBook()
    var  RESULT = BOOK.add(newBid(10, 5))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newBid(12, 4))
    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newAsk(8, 20))
    RESULT.takeSize    === 5 + 4
    RESULT.takeValue   === (10 * 5) + (12 * 4)
    RESULT.makers.size === 2
  }

  "one ask" should "take two equal size bids" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newBid(10, 13))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newBid(12, 7))
    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newAsk(8, 13 + 7))
    RESULT.takeSize    === 13 + 7
    RESULT.takeValue   === (10 * 13) + (12 * 7)
    RESULT.makers.size === 2
  }

  "one ask" should "take two larger size bids" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newBid(10, 15))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newBid(12, 21))
    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newAsk(8, 23))
    RESULT.takeSize    === 23
    RESULT.takeValue   === (12 * 21) + (10 * 2)
    RESULT.makers.size === 2
  }

  "one bid" should "take two smaller size asks" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newAsk(10, 5))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newAsk(12, 4))
    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newBid(13, 20))
    RESULT.takeSize    === 5 + 4
    RESULT.takeValue   === (10 * 5) + (12 * 4)
    RESULT.makers.size === 2
  }

  "one bid" should "take two equal size asks" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newAsk(10, 32))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newAsk(12, 64))
    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newBid(12, 32 + 64))
    RESULT.takeSize    === 32 + 64
    RESULT.takeValue   === (10 * 32) + (12 * 64)
    RESULT.makers.size === 2
  }

  "one bid" should "take two larger size asks" in {
    val BOOK   = new LimitOrderBook()
    var RESULT = BOOK.add(newAsk(10, 31))

    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newAsk(12, 33))
    RESULT.takeSize  === 0
    RESULT.takeValue === 0
    assert(RESULT.makers.isEmpty)

    RESULT = BOOK.add(newBid(12, 34))
    RESULT.takeSize    === 34
    RESULT.takeValue   === (10 * 31) + (12 * 3)
    RESULT.makers.size === 2
  }

}
