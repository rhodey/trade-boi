/*
 * Copyright (C) 2016 An Honest Effort LLC.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.anhonesteffort.btc.book;

import org.junit.Test;

import java.util.Optional;

public class LimitQueueTest {

  private Order newAsk(String orderId, double price, double size) {
    return new Order(orderId, Order.Side.ASK, price, size);
  }

  private Order newBid(String orderId, double price, double size) {
    return new Order(orderId, Order.Side.BID, price, size);
  }

  @Test
  public void testAddPeekRemoveAsks() {
    final LimitQueue ASKS = new LimitQueue(Order.Side.ASK);

    ASKS.addOrder(newAsk("00", 10, 1));
    ASKS.addOrder(newAsk("01", 10, 2));
    ASKS.addOrder(newAsk("02", 20, 2));
    ASKS.addOrder(newAsk("03",  5, 2));

    Optional<Limit> BEST_ASK = ASKS.peek();
    assert BEST_ASK.isPresent();
    assert BEST_ASK.get().getPrice()  == 5;
    assert BEST_ASK.get().getVolume() == 2;
    assert ASKS.removeOrder(5d, "03").isPresent();

    BEST_ASK = ASKS.peek();
    assert BEST_ASK.isPresent();
    assert BEST_ASK.get().getPrice()  == 10;
    assert BEST_ASK.get().getVolume() ==  3;
    assert ASKS.removeOrder(10d, "01").isPresent();

    BEST_ASK = ASKS.peek();
    assert BEST_ASK.isPresent();
    assert BEST_ASK.get().getPrice()  == 10;
    assert BEST_ASK.get().getVolume() ==  1;
    assert ASKS.removeOrder(20d, "02").isPresent();

    BEST_ASK = ASKS.peek();
    assert BEST_ASK.isPresent();
    assert BEST_ASK.get().getPrice()  == 10;
    assert BEST_ASK.get().getVolume() ==  1;
    assert ASKS.removeOrder(10d, "00").isPresent();

    assert !ASKS.peek().isPresent();
  }

  @Test
  public void testAddPeekRemoveBids() {
    final LimitQueue BIDS = new LimitQueue(Order.Side.BID);

    BIDS.addOrder(newBid("00", 10, 1));
    BIDS.addOrder(newBid("01", 10, 2));
    BIDS.addOrder(newBid("02", 20, 2));
    BIDS.addOrder(newBid("03",  5, 2));

    Optional<Limit> BEST_BID = BIDS.peek();
    assert BEST_BID.isPresent();
    assert BEST_BID.get().getPrice()  == 20;
    assert BEST_BID.get().getVolume() ==  2;
    assert BIDS.removeOrder(20d, "02").isPresent();

    BEST_BID = BIDS.peek();
    assert BEST_BID.isPresent();
    assert BEST_BID.get().getPrice()  == 10;
    assert BEST_BID.get().getVolume() ==  3;
    assert BIDS.removeOrder(10d, "01").isPresent();

    BEST_BID = BIDS.peek();
    assert BEST_BID.isPresent();
    assert BEST_BID.get().getPrice()  == 10;
    assert BEST_BID.get().getVolume() ==  1;
    assert BIDS.removeOrder(10d, "00").isPresent();

    BEST_BID = BIDS.peek();
    assert BEST_BID.isPresent();
    assert BEST_BID.get().getPrice()  == 5;
    assert BEST_BID.get().getVolume() == 2;
    assert BIDS.removeOrder(5d, "03").isPresent();

    assert !BIDS.peek().isPresent();
  }

}
