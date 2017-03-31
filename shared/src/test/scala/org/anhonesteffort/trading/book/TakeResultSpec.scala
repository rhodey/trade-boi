package org.anhonesteffort.trading.book

class TakeResultSpec extends BaseSpec {

  "TakeResult methods" should "work when empty" in {
    val MAKERS = Seq()
    val RESULT = TakeResult(newBid(10, 20), MAKERS, 0)

    RESULT.taker.getPrice === 10
    RESULT.taker.getSize  === 20
    RESULT.takeSize       ===  0
    RESULT.takeValue      ===  0
    assert(RESULT.makers.isEmpty)
  }

  "TakeResult methods" should "work with ASK makers" in {
    val ASK0   = newAsk(10, 1)
    val ASK1   = newAsk(10, 3)
    val ASK2   = newAsk(12, 2)

    ASK0.takeSize(ASK0.getSize)
    ASK1.takeSize(ASK1.getSize)
    ASK2.takeSize(ASK2.getSize)

    val TAKE_SIZE  = ASK0.getSize + ASK1.getSize + ASK2.getSize
    val TAKE_VALUE = ASK0.getValueRemoved + ASK1.getValueRemoved + ASK2.getValueRemoved
    val RESULT     = TakeResult(newBid(10, 20), Seq(ASK0, ASK1, ASK2), TAKE_SIZE)

    RESULT.takeSize    === TAKE_SIZE
    RESULT.takeValue   === TAKE_VALUE
    RESULT.makers.size === 3

    RESULT.clearMakerValueRemoved()

    ASK0.getValueRemoved === 0
    ASK1.getValueRemoved === 0
    ASK2.getValueRemoved === 0
  }

  "TakeResult methods" should "work with BID makers" in {
    val BID0 = newBid(12, 1)
    val BID1 = newBid(10, 3)
    val BID2 = newBid(10, 2)

    BID0.takeSize(BID0.getSize)
    BID1.takeSize(BID1.getSize)
    BID2.takeSize(BID2.getSize)

    val TAKE_SIZE  = BID0.getSize + BID1.getSize + BID2.getSize
    val TAKE_VALUE = BID0.getValueRemoved + BID1.getValueRemoved + BID2.getValueRemoved
    val RESULT     = TakeResult(newBid(10, 20), Seq(BID0, BID1, BID2), TAKE_SIZE)

    RESULT.takeSize    === TAKE_SIZE
    RESULT.takeValue   === TAKE_VALUE
    RESULT.makers.size === 3

    RESULT.clearMakerValueRemoved()

    BID0.getValueRemoved === 0
    BID1.getValueRemoved === 0
    BID2.getValueRemoved === 0
  }

}
