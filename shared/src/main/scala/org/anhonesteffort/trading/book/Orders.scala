package org.anhonesteffort.trading.book

object Orders {

  sealed trait Side
  case object Side {
    case object ASK extends Side
    case object BID extends Side
  }

  val SIDE_ASK = Side.ASK
  val SIDE_BID = Side.BID

  class Order(orderId: String, side: Side, price: Double, size: Double) {
    protected var sizeRemaining : Double = size
    protected var valueRemoved  : Double = 0d

    def isAsk: Boolean = side == Side.ASK
    def isBid: Boolean = side == Side.BID

    def getOrderId       : String = orderId
    def getSide          : Side   = side
    def getPrice         : Double = price
    def getSize          : Double = size
    def getSizeRemaining : Double = sizeRemaining
    def getValueRemoved  : Double = valueRemoved

    def clearValueRemoved() : Unit = {
      valueRemoved = 0d
    }

    def subtract(size: Double, price: Double): Unit = {
      sizeRemaining -= size
    }

    def takeSize(size: Double): Double = {
      val taken = Math.min(size, sizeRemaining)
      sizeRemaining -= taken
      valueRemoved  += price * taken
      taken
    }
  }

  def limitAsk(orderId: String, price: Double, size: Double): Order = {
    new Order(orderId, Side.ASK, price, size)
  }

  def limitBid(orderId: String, price: Double, size: Double): Order = {
    new Order(orderId, Side.BID, price, size)
  }

  class MarketOrder(orderId: String, side: Side, size: Double, funds: Double) extends Order(orderId, side, 0d, size) {
    private var fundsRemaining : Double = funds
    private var volumeRemoved  : Double = 0d

    def getFunds          : Double = funds
    def getFundsRemaining : Double = fundsRemaining
    def getVolumeRemoved  : Double = volumeRemoved

    override def subtract(size: Double, price: Double): Unit = {
      super.subtract(size, price)
      fundsRemaining -= (price * size)
      volumeRemoved  += size
    }

    def getSizeRemainingFor(price: Double): Double = {
      val fundsTakeSize = fundsRemaining / price

      if (funds > 0d && size > 0d) {
        Math.min(fundsTakeSize, sizeRemaining)
      } else if (funds > 0d) {
        fundsTakeSize
      } else if (size > 0d) {
        sizeRemaining
      } else {
        0d
      }
    }
  }

  def marketAsk(orderId: String, size: Double, funds: Double): MarketOrder = {
    new MarketOrder(orderId, Side.ASK, size, funds)
  }

  def marketBid(orderId: String, size: Double, funds: Double): MarketOrder = {
    new MarketOrder(orderId, Side.BID, size, funds)
  }

}
