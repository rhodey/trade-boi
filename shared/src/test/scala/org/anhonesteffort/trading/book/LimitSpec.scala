package org.anhonesteffort.trading.book

import org.anhonesteffort.trading.book.Orders.{MarketOrder, Order}

class LimitSpec extends BaseSpec {

  private def newOrder(orderId: String, size: Double): Order = {
    newBid(orderId, 1020, size)
  }

  private def newMarketOrder(orderId: String, size: Double, funds: Double): MarketOrder = {
    newMarketBid(orderId, size, funds)
  }

  "limit getters, add(), remove(), clear() and volume" should "work" in {
    val LIMIT = new Limit(1020)

    LIMIT.getPrice  === 1020
    LIMIT.getVolume === 0

    LIMIT.add(newOrder("00", 10))
    LIMIT.getVolume === 10

    LIMIT.add(newOrder("01", 20))
    LIMIT.getVolume === 30

    LIMIT.remove("00")
    LIMIT.getVolume === 20

    LIMIT.clear()
    assert(LIMIT.remove("01").isEmpty)
    LIMIT.getVolume === 0
  }

  "limit takeLiquidity() with no maker" should "work" in {
    val LIMIT   = new Limit(1020)
    val TAKER1  = newOrder("00", 10)
    val MAKERS1 = LIMIT.takeLiquidity(TAKER1)

    TAKER1.getSizeRemaining === 10
    MAKERS1.size            === 0
  }

  "market takeLiquidity() with no maker" should "work" in {
    val LIMIT   = new Limit(1020)
    val TAKER1  = newMarketOrder("00", 10, 20)
    val MAKERS1 = LIMIT.takeLiquidity(TAKER1)

    TAKER1.getVolumeRemoved === 0
    MAKERS1.size            === 0
  }
  
  "one full take one full make" should "work" in {
    val LIMIT = new Limit(1020)

    LIMIT.add(newOrder("00", 10))

    val TAKER1  = newOrder("01", 10)
    val MAKERS1 = LIMIT.takeLiquidity(TAKER1)

    TAKER1.getSizeRemaining       === 0
    MAKERS1.size                  === 1
    MAKERS1.head.getSizeRemaining === 0
    LIMIT.getVolume               === 0
  }
  
  "one full market size take one full make" should "work" in {
    val LIMIT = new Limit(1020)

    LIMIT.add(newOrder("00", 10))

    val TAKER1  = newMarketOrder("01", 10, -1)
    val MAKERS1 = LIMIT.takeLiquidity(TAKER1)

    TAKER1.getVolumeRemoved       === 10
    MAKERS1.size                  === 1
    MAKERS1.head.getSizeRemaining === 0
    LIMIT.getVolume               === 0
  }

  "one full market funds take one full make" should "work" in {
    val LIMIT = new Limit(1)

    LIMIT.add(newOrder("00", 10))

    val TAKER1  = newMarketOrder("01", -1, 10)
    val MAKERS1 = LIMIT.takeLiquidity(TAKER1)

    TAKER1.getVolumeRemoved       === 10
    MAKERS1.size                  === 1
    MAKERS1.head.getSizeRemaining === 0
    LIMIT.getVolume               === 0
  }

  "one full market size funds take one full make" should "work" in {
    val LIMIT = new Limit(1)

    LIMIT.add(newOrder("00", 12))

    val TAKER1  = newMarketOrder("01", 12, 20)
    val MAKERS1 = LIMIT.takeLiquidity(TAKER1)

    TAKER1.getVolumeRemoved       === 12
    MAKERS1.size                  ===  1
    MAKERS1.head.getSizeRemaining ===  0
    LIMIT.getVolume               ===  0
  }

  "full take partial  make" should "work" in {
    val LIMIT = new Limit(1020)

    LIMIT.add(newOrder("00", 10))

    val TAKER1  = newOrder("01", 8)
    val MAKERS1 = LIMIT.takeLiquidity(TAKER1)

    TAKER1.getSizeRemaining       === 0
    MAKERS1.size                  === 1
    MAKERS1.head.getSizeRemaining === 2
    LIMIT.getVolume               === 2
  }

  "full market size take partial make" should "work" in {
    val LIMIT = new Limit(1020)

    LIMIT.add(newOrder("00", 10))

    val TAKER1  = newMarketOrder("01", 8, -1)
    val MAKERS1 = LIMIT.takeLiquidity(TAKER1)

    TAKER1.getVolumeRemoved       === 8
    MAKERS1.size                  === 1
    MAKERS1.head.getSizeRemaining === 2
    LIMIT.getVolume               === 2
  }

  "full market funds take partial make" should "work" in {
    val LIMIT = new Limit(1)

    LIMIT.add(newOrder("00", 10))

    val TAKER1  = newMarketOrder("01", -1, 8)
    val MAKERS1 = LIMIT.takeLiquidity(TAKER1)

    TAKER1.getVolumeRemoved       === 8
    MAKERS1.size                  === 1
    MAKERS1.head.getSizeRemaining === 2
    LIMIT.getVolume               === 2
  }

  "full market size funds take partial make" should "work" in {
    val LIMIT = new Limit(1)

    LIMIT.add(newOrder("00", 10))

    val TAKER1  = newMarketOrder("01", 10, 8)
    val MAKERS1 = LIMIT.takeLiquidity(TAKER1)

    TAKER1.getVolumeRemoved       === 8
    MAKERS1.size                  === 1
    MAKERS1.head.getSizeRemaining === 2
    LIMIT.getVolume               === 2
  }

  "one full take one partial take" should "work" in {
    val LIMIT = new Limit(1020)

    LIMIT.add(newOrder("00", 10))

    val TAKER1  = newOrder("01", 8)
    val MAKERS1 = LIMIT.takeLiquidity(TAKER1)

    TAKER1.getSizeRemaining       === 0
    MAKERS1.size                  === 1
    MAKERS1.head.getSizeRemaining === 2
    LIMIT.getVolume               === 2

    val TAKER2  = newOrder("02", 4)
    val MAKERS2 = LIMIT.takeLiquidity(TAKER2)

    TAKER2.getSizeRemaining       === 2
    MAKERS2.size                  === 1
    MAKERS2.head.getSizeRemaining === 0
    LIMIT.getVolume               === 0
  }

  "two full makes one full take" should "work" in {
    val LIMIT = new Limit(1020)

    LIMIT.add(newOrder("00", 10))
    LIMIT.add(newOrder("01", 30))

    val TAKER1  = newOrder("02", 40)
    val MAKERS1 = LIMIT.takeLiquidity(TAKER1)

    TAKER1.getSizeRemaining       === 0
    MAKERS1.size                  === 2
    MAKERS1.head.getSizeRemaining === 0
    MAKERS1(1).getSizeRemaining   === 0
    LIMIT.getVolume               === 0
  }

  "one full make one partial make one full take" should "work" in {
    val LIMIT = new Limit(1020)

    LIMIT.add(newOrder("00", 10))
    LIMIT.add(newOrder("01", 30))

    val TAKER1  = newOrder("02", 30)
    val MAKERS1 = LIMIT.takeLiquidity(TAKER1)

    TAKER1.getSizeRemaining       ===  0
    MAKERS1.size                  ===  2
    MAKERS1.head.getSizeRemaining ===  0
    MAKERS1(1).getSizeRemaining   === 10
    LIMIT.getVolume               === 10
  }

}
