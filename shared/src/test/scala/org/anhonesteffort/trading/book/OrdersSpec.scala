package org.anhonesteffort.trading.book

import org.anhonesteffort.trading.book.Orders.{MarketOrder, Order, Side}
import org.scalatest.FlatSpec

class OrdersSpec extends FlatSpec {

  "limit order implementation" should "work" in {
    val ORDER = new Order("lol", Side.BID, 10, 20)

    ORDER.getOrderId       === "lol"
    ORDER.getSide          === Side.BID
    ORDER.getPrice         === 10
    ORDER.getSize          === 20
    ORDER.getSizeRemaining === 20
    ORDER.getValueRemoved  ===  0

    ORDER.takeSize(5)      ===  5
    ORDER.getSizeRemaining === 15
    ORDER.getValueRemoved  ===  5 * ORDER.getPrice

    ORDER.clearValueRemoved()
    ORDER.getValueRemoved === 0

    ORDER.takeSize(20)     === 15
    ORDER.getSizeRemaining ===  0
    ORDER.getValueRemoved  === 15 * ORDER.getPrice

    ORDER.clearValueRemoved()
    ORDER.getValueRemoved === 0
  }

  "market order implementation" should "work with size" in {
    val ORDER = new MarketOrder("lol", Side.BID, 100, -1)

    ORDER.getOrderId       === "lol"
    ORDER.getSide          === Side.BID
    ORDER.getPrice         ===   0
    ORDER.getSize          === 100
    ORDER.getSizeRemaining === 100
    ORDER.getValueRemoved  ===   0

    assert(ORDER.getFunds             < 0)
    assert(ORDER.getFundsRemaining    < 0)
    ORDER.getVolumeRemoved          === 0
    ORDER.getSizeRemainingFor(1337) === 100

    ORDER.subtract(75, 1337)

    ORDER.getVolumeRemoved          === 75
    ORDER.getSizeRemainingFor(1337) === 25

    ORDER.subtract(25, 31337)

    ORDER.getVolumeRemoved           === 100
    ORDER.getSizeRemainingFor(31337) ===   0
    ORDER.getSizeRemaining           ===   0
    ORDER.getValueRemoved            ===   0
  }

  "market order implementation" should "work with funds" in {
    val ORDER = new MarketOrder("lol", Side.BID, -1, 100)

    ORDER.getOrderId              === "lol"
    ORDER.getSide                 === Side.BID
    ORDER.getPrice                === 0
    assert(ORDER.getSize            < 0)
    assert(ORDER.getSizeRemaining   < 0)
    ORDER.getValueRemoved         === 0

    ORDER.getFunds                === 100
    ORDER.getFundsRemaining       === 100
    ORDER.getVolumeRemoved        === 0
    ORDER.getSizeRemainingFor(25) === 4

    ORDER.subtract(3, 25)

    ORDER.getVolumeRemoved        === 3
    ORDER.getSizeRemainingFor(25) === 1

    ORDER.subtract(1, 25)

    ORDER.getVolumeRemoved       === 4
    ORDER.getSizeRemainingFor(1) === 0
    assert(ORDER.getSizeRemaining  < 0)
    ORDER.getValueRemoved        === 0
  }

  "market order implementation" should "work with size and funds" in {
    val ORDER = new MarketOrder("lol", Side.BID, 100, 50)
    ORDER.getOrderId       === "lol"
    ORDER.getSide          === Side.BID
    ORDER.getPrice         ===   0
    ORDER.getSize          === 100
    ORDER.getSizeRemaining === 100
    ORDER.getValueRemoved  ===   0

    ORDER.getFunds               === 50
    ORDER.getFundsRemaining      === 50
    ORDER.getVolumeRemoved       ===  0
    ORDER.getSizeRemainingFor(1) === 50
    ORDER.getSizeRemainingFor(5) === (50 / 5)

    ORDER.subtract(25, 1)

    ORDER.getVolumeRemoved       === 25
    ORDER.getSizeRemainingFor(5) === (25 / 5)

    ORDER.subtract(10, 2)

    ORDER.getVolumeRemoved       === 35
    ORDER.getSizeRemainingFor(1) === 5
    ORDER.getSizeRemaining       === 100 - (25 + 10)
    ORDER.getValueRemoved        === 0

    ORDER.subtract(5, 1)

    ORDER.getVolumeRemoved       === 40
    ORDER.getSizeRemainingFor(1) === 0
    ORDER.getSizeRemaining       === 100 - (25 + 10 + 5)
    ORDER.getValueRemoved        === 0
  }

}
