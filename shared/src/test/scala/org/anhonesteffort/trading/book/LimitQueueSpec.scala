package org.anhonesteffort.trading.book

import org.anhonesteffort.trading.book.Orders.Side

class LimitQueueSpec extends BaseSpec {

  "add, peek, remove, clear asks" should "work" in {
    val ASKS = new LimitQueue(Side.ASK)

    ASKS.add(newAsk("00", 10, 1))
    ASKS.add(newAsk("01", 10, 2))
    ASKS.add(newAsk("02", 20, 2))
    ASKS.add(newAsk("03",  5, 2))

    var BEST_ASK = ASKS.peek
    assert(BEST_ASK.isDefined)
    BEST_ASK.get.getPrice  === 5
    BEST_ASK.get.getVolume === 2
    assert(ASKS.remove(5d, "03").isDefined)

    BEST_ASK = ASKS.peek
    assert(BEST_ASK.isDefined)
    BEST_ASK.get.getPrice  === 10
    BEST_ASK.get.getVolume ===  3
    assert(ASKS.remove(10d, "01").isDefined)

    BEST_ASK = ASKS.peek
    assert(BEST_ASK.isDefined)
    BEST_ASK.get.getPrice  === 10
    BEST_ASK.get.getVolume ===  1
    assert(ASKS.remove(20d, "02").isDefined)

    BEST_ASK = ASKS.peek
    assert(BEST_ASK.isDefined)
    BEST_ASK.get.getPrice  === 10
    BEST_ASK.get.getVolume ===  1

    ASKS.clear()
    assert(ASKS.remove(10d, "00").isEmpty)
    assert(ASKS.peek.isEmpty)
  }

  "add, peek, remove, clear bids" should "work" in {
    val BIDS = new LimitQueue(Side.BID)

    BIDS.add(newBid("00", 10, 1))
    BIDS.add(newBid("01", 10, 2))
    BIDS.add(newBid("02", 20, 2))
    BIDS.add(newBid("03",  5, 2))

    var BEST_BID = BIDS.peek
    assert(BEST_BID.isDefined)
    BEST_BID.get.getPrice  === 20
    BEST_BID.get.getVolume ===  2
    assert(BIDS.remove(20d, "02").isDefined)

    BEST_BID = BIDS.peek
    assert(BEST_BID.isDefined)
    BEST_BID.get.getPrice  === 10
    BEST_BID.get.getVolume ===  3
    assert(BIDS.remove(10d, "01").isDefined)

    BEST_BID = BIDS.peek
    assert(BEST_BID.isDefined)
    BEST_BID.get.getPrice  === 10
    BEST_BID.get.getVolume ===  1
    assert(BIDS.remove(10d, "00").isDefined)

    BEST_BID = BIDS.peek
    assert(BEST_BID.isDefined)
    BEST_BID.get.getPrice  === 5
    BEST_BID.get.getVolume === 2

    BIDS.clear()
    assert(BIDS.remove(5d, "03").isEmpty)
    assert(BIDS.peek.isEmpty)
  }

  "remove ask liquidity" should "work" in {
    val ASKS = new LimitQueue(Side.ASK)
    var BID  = newBid(15, 5)

    ASKS.add(newAsk(10, 1))
    ASKS.add(newAsk(10, 2))
    ASKS.add(newAsk(20, 2))
    ASKS.add(newAsk(5, 2))

    var MAKERS = ASKS.takeLiquidityFromBestLimit(BID)
    BID.getSizeRemaining         === 3
    MAKERS.size                  === 1
    MAKERS.head.getPrice         === 5
    MAKERS.head.getSizeRemaining === 0

    MAKERS = ASKS.takeLiquidityFromBestLimit(BID)
    BID.getSizeRemaining         ===  0
    MAKERS.size                  ===  2
    MAKERS.head.getPrice         === 10
    MAKERS.head.getSizeRemaining ===  0
    MAKERS(1).getPrice           === 10
    MAKERS(1).getSizeRemaining   ===  0

    MAKERS = ASKS.takeLiquidityFromBestLimit(BID)
    assert(MAKERS.isEmpty)

    BID    = newBid(20, 3)
    MAKERS = ASKS.takeLiquidityFromBestLimit(BID)
    BID.getSizeRemaining         ===  1
    MAKERS.size                  ===  1
    MAKERS.head.getPrice         === 20
    MAKERS.head.getSizeRemaining ===  0

    MAKERS = ASKS.takeLiquidityFromBestLimit(BID)
    assert(MAKERS.isEmpty)
    assert(ASKS.peek.isEmpty)
  }

  "remove bid liquidity" should "work" in {
    val BIDS = new LimitQueue(Side.BID)
    var ASK  = newAsk(15, 5)

    BIDS.add(newBid(10, 1))
    BIDS.add(newBid(10, 2))
    BIDS.add(newBid(20, 2))
    BIDS.add(newBid(5, 2))

    var MAKERS = BIDS.takeLiquidityFromBestLimit(ASK)
    ASK.getSizeRemaining         ===  3
    MAKERS.size                  ===  1
    MAKERS.head.getPrice         === 20
    MAKERS.head.getSizeRemaining ===  0

    MAKERS = BIDS.takeLiquidityFromBestLimit(ASK)
    ASK.getSizeRemaining === 3
    assert(MAKERS.isEmpty)

    ASK    = newAsk(5, 5)
    MAKERS = BIDS.takeLiquidityFromBestLimit(ASK)
    ASK.getSizeRemaining         ===  2
    MAKERS.size                  ===  2
    MAKERS.head.getPrice         === 10
    MAKERS.head.getSizeRemaining ===  0
    MAKERS(1).getPrice           === 10
    MAKERS(1).getSizeRemaining   ===  0

    MAKERS = BIDS.takeLiquidityFromBestLimit(ASK)
    ASK.getSizeRemaining         === 0
    MAKERS.size                  === 1
    MAKERS.head.getPrice         === 5
    MAKERS.head.getSizeRemaining === 0

    MAKERS = BIDS.takeLiquidityFromBestLimit(ASK)
    assert(MAKERS.isEmpty)
    assert(BIDS.peek.isEmpty)
  }

  "remove ask liquidity with market bids" should "work" in {
    val ASKS = new LimitQueue(Side.ASK)
    var BID  = newMarketBid(5, -1)

    ASKS.add(newAsk(10, 1))
    ASKS.add(newAsk(10, 2))
    ASKS.add(newAsk(20, 2))
    ASKS.add(newAsk(5, 2))

    var MAKERS = ASKS.takeLiquidityFromBestLimit(BID)

    BID.getVolumeRemoved         === 2
    MAKERS.size                  === 1
    MAKERS.head.getPrice         === 5
    MAKERS.head.getSizeRemaining === 0

    MAKERS = ASKS.takeLiquidityFromBestLimit(BID)
    BID.getVolumeRemoved         ===  5
    MAKERS.size                  ===  2
    MAKERS.head.getPrice         === 10
    MAKERS.head.getSizeRemaining ===  0
    MAKERS(1).getPrice           === 10
    MAKERS(1).getSizeRemaining   ===  0

    MAKERS = ASKS.takeLiquidityFromBestLimit(BID)
    assert(MAKERS.isEmpty)

    BID    = newMarketBid(3, -1)
    MAKERS = ASKS.takeLiquidityFromBestLimit(BID)
    BID.getVolumeRemoved         ===  2
    BID.getSizeRemaining         ===  1
    MAKERS.size                  ===  1
    MAKERS.head.getPrice         === 20
    MAKERS.head.getSizeRemaining ===  0

    MAKERS = ASKS.takeLiquidityFromBestLimit(BID)
    assert(MAKERS.isEmpty)
    assert(ASKS.peek.isEmpty)
  }

  "remove bid liquidity with market asks" should "work" in {
    val BIDS = new LimitQueue(Side.BID)
    var ASK  = newMarketAsk(5, -1)

    BIDS.add(newBid(10, 1))
    BIDS.add(newBid(10, 2))
    BIDS.add(newBid(20, 2))
    BIDS.add(newBid(5, 2))

    var MAKERS = BIDS.takeLiquidityFromBestLimit(ASK)

    ASK.getVolumeRemoved         ===  2
    MAKERS.size                  ===  1
    MAKERS.head.getPrice         === 20
    MAKERS.head.getSizeRemaining ===  0

    MAKERS = BIDS.takeLiquidityFromBestLimit(ASK)
    ASK.getVolumeRemoved         ===  5
    ASK.getSizeRemaining         ===  0
    MAKERS.size                  ===  2
    MAKERS.head.getPrice         === 10
    MAKERS.head.getSizeRemaining ===  0
    MAKERS(1).getPrice           === 10
    MAKERS(1).getSizeRemaining   ===  0

    MAKERS = BIDS.takeLiquidityFromBestLimit(ASK)
    assert(MAKERS.isEmpty)

    ASK    = newMarketAsk(3, -1)
    MAKERS = BIDS.takeLiquidityFromBestLimit(ASK)
    ASK.getVolumeRemoved         === 2
    ASK.getSizeRemaining         === 1
    MAKERS.size                  === 1
    MAKERS.head.getPrice         === 5
    MAKERS.head.getSizeRemaining === 0

    MAKERS = BIDS.takeLiquidityFromBestLimit(ASK)
    assert(MAKERS.isEmpty)
    assert(BIDS.peek.isEmpty)
  }

}
