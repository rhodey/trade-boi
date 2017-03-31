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

package org.anhonesteffort.trading.book;

import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class LimitQueueTest extends BaseTest {

  @Test
  public void testAddPeekRemoveClearAsks() {
    final LimitQueue ASKS = new LimitQueue(Order.Side.ASK, 10);

    ASKS.addOrder(newAsk("00", 10, 1));
    ASKS.addOrder(newAsk("01", 10, 2));
    ASKS.addOrder(newAsk("02", 20, 2));
    ASKS.addOrder(newAsk("03",  5, 2));

    Optional<Limit> BEST_ASK = ASKS.peek();
    assertTrue(BEST_ASK.isPresent());
    assertTrue(BEST_ASK.get().getPrice()  == 5);
    assertTrue(BEST_ASK.get().getVolume() == 2);
    assertTrue(ASKS.removeOrder(5d, "03").isPresent());

    BEST_ASK = ASKS.peek();
    assertTrue(BEST_ASK.isPresent());
    assertTrue(BEST_ASK.get().getPrice()  == 10);
    assertTrue(BEST_ASK.get().getVolume() ==  3);
    assertTrue(ASKS.removeOrder(10d, "01").isPresent());

    BEST_ASK = ASKS.peek();
    assertTrue(BEST_ASK.isPresent());
    assertTrue(BEST_ASK.get().getPrice()  == 10);
    assertTrue(BEST_ASK.get().getVolume() ==  1);
    assertTrue(ASKS.removeOrder(20d, "02").isPresent());

    BEST_ASK = ASKS.peek();
    assertTrue(BEST_ASK.isPresent());
    assertTrue(BEST_ASK.get().getPrice()  == 10);
    assertTrue(BEST_ASK.get().getVolume() ==  1);

    ASKS.clear();
    assertTrue(!ASKS.removeOrder(10d, "00").isPresent());
    assertTrue(!ASKS.peek().isPresent());
  }

  @Test
  public void testAddPeekRemoveClearBids() {
    final LimitQueue BIDS = new LimitQueue(Order.Side.BID, 10);

    BIDS.addOrder(newBid("00", 10, 1));
    BIDS.addOrder(newBid("01", 10, 2));
    BIDS.addOrder(newBid("02", 20, 2));
    BIDS.addOrder(newBid("03",  5, 2));

    Optional<Limit> BEST_BID = BIDS.peek();
    assertTrue(BEST_BID.isPresent());
    assertTrue(BEST_BID.get().getPrice()  == 20);
    assertTrue(BEST_BID.get().getVolume() ==  2);
    assertTrue(BIDS.removeOrder(20d, "02").isPresent());

    BEST_BID = BIDS.peek();
    assertTrue(BEST_BID.isPresent());
    assertTrue(BEST_BID.get().getPrice()  == 10);
    assertTrue(BEST_BID.get().getVolume() ==  3);
    assertTrue(BIDS.removeOrder(10d, "01").isPresent());

    BEST_BID = BIDS.peek();
    assertTrue(BEST_BID.isPresent());
    assertTrue(BEST_BID.get().getPrice()  == 10);
    assertTrue(BEST_BID.get().getVolume() ==  1);
    assertTrue(BIDS.removeOrder(10d, "00").isPresent());

    BEST_BID = BIDS.peek();
    assertTrue(BEST_BID.isPresent());
    assertTrue(BEST_BID.get().getPrice()  == 5);
    assertTrue(BEST_BID.get().getVolume() == 2);

    BIDS.clear();
    assertTrue(!BIDS.removeOrder(5d, "03").isPresent());
    assertTrue(!BIDS.peek().isPresent());
  }

  @Test
  public void testRemoveAskLiquidity() {
    final LimitQueue ASKS = new LimitQueue(Order.Side.ASK, 10);
          Order      BID  = newBid(15, 5);

    ASKS.addOrder(newAsk(10, 1));
    ASKS.addOrder(newAsk(10, 2));
    ASKS.addOrder(newAsk(20, 2));
    ASKS.addOrder(newAsk(5, 2));

    List<Order> MAKERS = ASKS.takeLiquidityFromBestLimit(BID);
    assertTrue(BID.getSizeRemaining()           == 3);
    assertTrue(MAKERS.size()                    == 1);
    assertTrue(MAKERS.get(0).getPrice()         == 5);
    assertTrue(MAKERS.get(0).getSizeRemaining() == 0);

    MAKERS = ASKS.takeLiquidityFromBestLimit(BID);
    assertTrue(BID.getSizeRemaining()           ==  0);
    assertTrue(MAKERS.size()                    ==  2);
    assertTrue(MAKERS.get(0).getPrice()         == 10);
    assertTrue(MAKERS.get(0).getSizeRemaining() ==  0);
    assertTrue(MAKERS.get(1).getPrice()         == 10);
    assertTrue(MAKERS.get(1).getSizeRemaining() ==  0);

    MAKERS = ASKS.takeLiquidityFromBestLimit(BID);
    assertTrue(MAKERS.size() == 0);

    BID    = newBid(20, 3);
    MAKERS = ASKS.takeLiquidityFromBestLimit(BID);
    assertTrue(BID.getSizeRemaining()           ==  1);
    assertTrue(MAKERS.size()                    ==  1);
    assertTrue(MAKERS.get(0).getPrice()         == 20);
    assertTrue(MAKERS.get(0).getSizeRemaining() ==  0);

    MAKERS = ASKS.takeLiquidityFromBestLimit(BID);
    assertTrue(MAKERS.size() == 0);
    assertTrue(!ASKS.peek().isPresent());
  }

  @Test
  public void testRemoveBidLiquidity() {
    final LimitQueue BIDS = new LimitQueue(Order.Side.BID, 10);
          Order      ASK  = newAsk(15, 5);

    BIDS.addOrder(newBid(10, 1));
    BIDS.addOrder(newBid(10, 2));
    BIDS.addOrder(newBid(20, 2));
    BIDS.addOrder(newBid(5, 2));

    List<Order> MAKERS = BIDS.takeLiquidityFromBestLimit(ASK);
    assertTrue(ASK.getSizeRemaining()           ==  3);
    assertTrue(MAKERS.size()                    ==  1);
    assertTrue(MAKERS.get(0).getPrice()         == 20);
    assertTrue(MAKERS.get(0).getSizeRemaining() ==  0);

    MAKERS = BIDS.takeLiquidityFromBestLimit(ASK);
    assertTrue(ASK.getSizeRemaining() == 3);
    assertTrue(MAKERS.size()          == 0);

    ASK    = newAsk(5, 5);
    MAKERS = BIDS.takeLiquidityFromBestLimit(ASK);
    assertTrue(ASK.getSizeRemaining()           ==  2);
    assertTrue(MAKERS.size()                    ==  2);
    assertTrue(MAKERS.get(0).getPrice()         == 10);
    assertTrue(MAKERS.get(0).getSizeRemaining() ==  0);
    assertTrue(MAKERS.get(1).getPrice()         == 10);
    assertTrue(MAKERS.get(1).getSizeRemaining() ==  0);

    MAKERS = BIDS.takeLiquidityFromBestLimit(ASK);
    assertTrue(ASK.getSizeRemaining()           == 0);
    assertTrue(MAKERS.size()                    == 1);
    assertTrue(MAKERS.get(0).getPrice()         == 5);
    assertTrue(MAKERS.get(0).getSizeRemaining() == 0);

    MAKERS = BIDS.takeLiquidityFromBestLimit(ASK);
    assertTrue(MAKERS.size() == 0);
    assertTrue(!BIDS.peek().isPresent());
  }

  @Test
  public void testRemoveAskLiquidityWithMarketBids() {
    final LimitQueue  ASKS = new LimitQueue(Order.Side.ASK, 10);
          MarketOrder BID  = newMarketBid(5, -1);

    ASKS.addOrder(newAsk(10, 1));
    ASKS.addOrder(newAsk(10, 2));
    ASKS.addOrder(newAsk(20, 2));
    ASKS.addOrder(newAsk(5, 2));

    List<Order> MAKERS = ASKS.takeLiquidityFromBestLimit(BID);

    assertTrue(BID.getVolumeRemoved()           == 2);
    assertTrue(MAKERS.size()                    == 1);
    assertTrue(MAKERS.get(0).getPrice()         == 5);
    assertTrue(MAKERS.get(0).getSizeRemaining() == 0);

    MAKERS = ASKS.takeLiquidityFromBestLimit(BID);
    assertTrue(BID.getVolumeRemoved()           ==  5);
    assertTrue(MAKERS.size()                    ==  2);
    assertTrue(MAKERS.get(0).getPrice()         == 10);
    assertTrue(MAKERS.get(0).getSizeRemaining() ==  0);
    assertTrue(MAKERS.get(1).getPrice()         == 10);
    assertTrue(MAKERS.get(1).getSizeRemaining() ==  0);

    MAKERS = ASKS.takeLiquidityFromBestLimit(BID);
    assertTrue(MAKERS.size() == 0);

    BID    = newMarketBid(3, -1);
    MAKERS = ASKS.takeLiquidityFromBestLimit(BID);
    assertTrue(BID.getVolumeRemoved()           ==  2);
    assertTrue(BID.getSizeRemaining()           ==  1);
    assertTrue(MAKERS.size()                    ==  1);
    assertTrue(MAKERS.get(0).getPrice()         == 20);
    assertTrue(MAKERS.get(0).getSizeRemaining() ==  0);

    MAKERS = ASKS.takeLiquidityFromBestLimit(BID);
    assertTrue(MAKERS.size() == 0);
    assertTrue(!ASKS.peek().isPresent());
  }

  @Test
  public void testRemoveBidLiquidityWithMarketAsks() {
    final LimitQueue  BIDS = new LimitQueue(Order.Side.BID, 10);
          MarketOrder ASK  = newMarketAsk(5, -1);

    BIDS.addOrder(newBid(10, 1));
    BIDS.addOrder(newBid(10, 2));
    BIDS.addOrder(newBid(20, 2));
    BIDS.addOrder(newBid(5, 2));

    List<Order> MAKERS = BIDS.takeLiquidityFromBestLimit(ASK);

    assertTrue(ASK.getVolumeRemoved()           ==  2);
    assertTrue(MAKERS.size()                    ==  1);
    assertTrue(MAKERS.get(0).getPrice()         == 20);
    assertTrue(MAKERS.get(0).getSizeRemaining() ==  0);

    MAKERS = BIDS.takeLiquidityFromBestLimit(ASK);
    assertTrue(ASK.getVolumeRemoved()           ==  5);
    assertTrue(ASK.getSizeRemaining()           ==  0);
    assertTrue(MAKERS.size()                    ==  2);
    assertTrue(MAKERS.get(0).getPrice()         == 10);
    assertTrue(MAKERS.get(0).getSizeRemaining() ==  0);
    assertTrue(MAKERS.get(1).getPrice()         == 10);
    assertTrue(MAKERS.get(1).getSizeRemaining() ==  0);

    MAKERS = BIDS.takeLiquidityFromBestLimit(ASK);
    assertTrue(MAKERS.size() == 0);

    ASK    = newMarketAsk(3, -1);
    MAKERS = BIDS.takeLiquidityFromBestLimit(ASK);
    assertTrue(ASK.getVolumeRemoved()           == 2);
    assertTrue(ASK.getSizeRemaining()           == 1);
    assertTrue(MAKERS.size()                    == 1);
    assertTrue(MAKERS.get(0).getPrice()         == 5);
    assertTrue(MAKERS.get(0).getSizeRemaining() == 0);

    MAKERS = BIDS.takeLiquidityFromBestLimit(ASK);
    assertTrue(MAKERS.size() == 0);
    assertTrue(!BIDS.peek().isPresent());
  }

}
