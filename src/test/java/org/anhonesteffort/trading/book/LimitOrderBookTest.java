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
import static org.junit.Assert.assertTrue;

public class LimitOrderBookTest extends BaseTest {

  @Test
  public void testAddRemoveClearAsk() {
    final LimitOrderBook BOOK = new LimitOrderBook(10);

    BOOK.add(newAsk("00", 10, 20));
    BOOK.add(newAsk("01", 30, 40));

    assertTrue(BOOK.remove(Order.Side.ASK, 10d, "00").isPresent());
    BOOK.clear();
    assertTrue(!BOOK.remove(Order.Side.ASK, 30d, "01").isPresent());
  }

  @Test
  public void testAddRemoveClearBid() {
    final LimitOrderBook BOOK = new LimitOrderBook(10);

    BOOK.add(newBid("00", 10, 20));
    BOOK.add(newBid("01", 30, 40));

    assertTrue(BOOK.remove(Order.Side.BID, 10d, "00").isPresent());
    BOOK.clear();
    assertTrue(!BOOK.remove(Order.Side.BID, 30d, "01").isPresent());
  }

  @Test
  public void testAskWontTakeEmptyBook() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
    final TakeResult     RESULT = BOOK.add(newAsk(10, 10));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());
  }

  @Test
  public void testBidWontTakeEmptyBook() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
    final TakeResult     RESULT = BOOK.add(newBid(10, 10));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());
  }

  @Test
  public void testMarketAskWontTakeEmptyBook() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
    final MarketOrder    TAKER  = newMarketAsk("00", 10, 20);
    final TakeResult     RESULT = BOOK.add(TAKER);

    assertTrue(RESULT.getTakeSize()      == 0);
    assertTrue(RESULT.getTakeValue()     == 0);
    assertTrue(RESULT.getMakers().size() == 0);

    assertTrue(!BOOK.remove(Order.Side.ASK, 10d, "00").isPresent());
    assertTrue(!BOOK.remove(Order.Side.ASK, 20d, "00").isPresent());
  }

  @Test
  public void testMarketBidWontTakeEmptyBook() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
    final MarketOrder    TAKER  = newMarketBid("00", 10, 20);
    final TakeResult     RESULT = BOOK.add(TAKER);

    assertTrue(RESULT.getTakeSize()      == 0);
    assertTrue(RESULT.getTakeValue()     == 0);
    assertTrue(RESULT.getMakers().size() == 0);

    assertTrue(!BOOK.remove(Order.Side.BID, 10d, "00").isPresent());
    assertTrue(!BOOK.remove(Order.Side.BID, 20d, "00").isPresent());
  }

  @Test
  public void testAskWontTakeSmallerBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newBid(8, 10));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newAsk(9, 10));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());
  }

  @Test
  public void testBidWontTakeLargerAsk() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newAsk(8, 10));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newBid(7, 10));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());
  }

  @Test
  public void testOneAskTakesOneSmallerSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newBid(10, 5));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newAsk(10, 20));
    assertTrue(RESULT.getTakeSize()      == 5);
    assertTrue(RESULT.getTakeValue()     == 10 * 5);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testOneAskTakesOneEqualSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newBid(10, 5));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newAsk(10, 5));
    assertTrue(RESULT.getTakeSize()      == 5);
    assertTrue(RESULT.getTakeValue()     == 10 * 5);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testOneAskTakesOneLargerSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newBid(10, 15));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newAsk(10, 5));
    assertTrue(RESULT.getTakeSize()      == 5);
    assertTrue(RESULT.getTakeValue()     == 10 * 5);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testOneBidTakesOneSmallerSizeAsk() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newAsk(10, 5));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newBid(10, 8));
    assertTrue(RESULT.getTakeSize()      == 5);
    assertTrue(RESULT.getTakeValue()     == 10 * 5);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testOneBidTakesOneEqualSizeAsk() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newAsk(10, 5));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newBid(10, 5));
    assertTrue(RESULT.getTakeSize()      == 5);
    assertTrue(RESULT.getTakeValue()     == 10 * 5);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testOneBidTakesOneLargerSizeAsk() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newAsk(10, 5));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newBid(10, 2));
    assertTrue(RESULT.getTakeSize()      == 2);
    assertTrue(RESULT.getTakeValue()     == 10 * 2);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testOneMarketSizeAskTakesOneSmallerSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newBid(10, 5));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newMarketAsk(10, -1));
    assertTrue(RESULT.getTakeSize()      == 5);
    assertTrue(RESULT.getTakeValue()     == 10 * 5);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testOneMarketSizeAskTakesOneEqualSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newBid(10, 5));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newMarketAsk(5, -1));
    assertTrue(RESULT.getTakeSize()      == 5);
    assertTrue(RESULT.getTakeValue()     == 10 * 5);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testOneMarketSizeAskTakesOneLargerSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newBid(10, 8));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newMarketAsk(5, -1));
    assertTrue(RESULT.getTakeSize()      == 5);
    assertTrue(RESULT.getTakeValue()     == 10 * 5);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testOneMarketFundsAskTakesOneSmallerSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newBid(1, 5));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newMarketAsk(-1, 10));
    assertTrue(RESULT.getTakeSize()      == 5);
    assertTrue(RESULT.getTakeValue()     == 5);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testOneMarketFundsAskTakesOneEqualSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newBid(1, 5));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newMarketAsk(-1, 5));
    assertTrue(RESULT.getTakeSize()      == 5);
    assertTrue(RESULT.getTakeValue()     == 5);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testOneMarketFundsAskTakesOneLargerSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newBid(1, 10));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newMarketAsk(-1, 5));
    assertTrue(RESULT.getTakeSize()      == 5);
    assertTrue(RESULT.getTakeValue()     == 5);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testOneMarketSizeBidTakesOneSmallerSizeAsk() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newAsk(10, 5));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newMarketBid(10, -1));
    assertTrue(RESULT.getTakeSize()      == 5);
    assertTrue(RESULT.getTakeValue()     == (10 * 5));
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testOneMarketSizeBidTakesOneEqualSizeAsk() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newAsk(10, 10));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newMarketBid(10, -1));
    assertTrue(RESULT.getTakeSize()      == 10);
    assertTrue(RESULT.getTakeValue()     == (10 * 10));
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testOneMarketSizeBidTakesOneLargerSizeAsk() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newAsk(10, 10));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newMarketBid(5, -1));
    assertTrue(RESULT.getTakeSize()      == 5);
    assertTrue(RESULT.getTakeValue()     == (10 * 5));
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testTwoAsksTakesOneSmallerSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newBid(10, 20));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newAsk(10, 5));
    assertTrue(RESULT.getTakeSize()      == 5);
    assertTrue(RESULT.getTakeValue()     == 10 * 5);
    assertTrue(RESULT.getMakers().size() == 1);
    RESULT.clearMakerValueRemoved();

    RESULT = BOOK.add(newAsk(10, 25));
    assertTrue(RESULT.getTakeSize()      == 15);
    assertTrue(RESULT.getTakeValue()     == 10 * 15);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testTwoAsksTakesOneEqualSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newBid(10, 20));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newAsk(10, 12));
    assertTrue(RESULT.getTakeSize()      == 12);
    assertTrue(RESULT.getTakeValue()     == 10 * 12);
    assertTrue(RESULT.getMakers().size() == 1);
    RESULT.clearMakerValueRemoved();

    RESULT = BOOK.add(newAsk(10, 8));
    assertTrue(RESULT.getTakeSize()      == 8);
    assertTrue(RESULT.getTakeValue()     == 10 * 8);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testTwoAsksTakesOneLargerSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newBid(10, 30));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newAsk(10, 12));
    assertTrue(RESULT.getTakeSize()      == 12);
    assertTrue(RESULT.getTakeValue()     == 10 * 12);
    assertTrue(RESULT.getMakers().size() == 1);
    RESULT.clearMakerValueRemoved();

    RESULT = BOOK.add(newAsk(10, 8));
    assertTrue(RESULT.getTakeSize()      == 8);
    assertTrue(RESULT.getTakeValue()     == 10 * 8);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testTwoBidsTakesOneSmallerSizeAsk() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newAsk(10, 20));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newBid(10, 5));
    assertTrue(RESULT.getTakeSize()      == 5);
    assertTrue(RESULT.getTakeValue()     == 10 * 5);
    assertTrue(RESULT.getMakers().size() == 1);
    RESULT.clearMakerValueRemoved();

    RESULT = BOOK.add(newBid(10, 25));
    assertTrue(RESULT.getTakeSize()      == 15);
    assertTrue(RESULT.getTakeValue()     == 10 * 15);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testTwoBidsTakesOneEqualSizeAsk() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newAsk(10, 20));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newBid(10, 9));
    assertTrue(RESULT.getTakeSize()      == 9);
    assertTrue(RESULT.getTakeValue()     == 10 * 9);
    assertTrue(RESULT.getMakers().size() == 1);
    RESULT.clearMakerValueRemoved();

    RESULT = BOOK.add(newBid(10, 11));
    assertTrue(RESULT.getTakeSize()      == 11);
    assertTrue(RESULT.getTakeValue()     == 10 * 11);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testTwoBidsTakesOneLargerSizeAsk() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newAsk(10, 30));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newBid(10, 5));
    assertTrue(RESULT.getTakeSize()      == 5);
    assertTrue(RESULT.getTakeValue()     == 10 * 5);
    assertTrue(RESULT.getMakers().size() == 1);
    RESULT.clearMakerValueRemoved();

    RESULT = BOOK.add(newBid(10, 6));
    assertTrue(RESULT.getTakeSize()      == 6);
    assertTrue(RESULT.getTakeValue()     == 10 * 6);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testTwoMarketAsksTakesOneSmallerSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newBid(20, 15));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newMarketAsk(10, -1));
    assertTrue(RESULT.getTakeSize()      == 10);
    assertTrue(RESULT.getTakeValue()     == 10 * 20);
    assertTrue(RESULT.getMakers().size() == 1);
    RESULT.clearMakerValueRemoved();

    RESULT = BOOK.add(newMarketAsk(10, -1));
    assertTrue(RESULT.getTakeSize()      == 5);
    assertTrue(RESULT.getTakeValue()     == 5 * 20);
    assertTrue(RESULT.getMakers().size() == 1);
  }

  @Test
  public void testOneAskTakesTwoSmallerSizeBids() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newBid(10, 5));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newBid(12, 4));
    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newAsk(8, 20));
    assertTrue(RESULT.getTakeSize()      == 5 + 4);
    assertTrue(RESULT.getTakeValue()     == (10 * 5) + (12 * 4));
    assertTrue(RESULT.getMakers().size() == 2);
  }

  @Test
  public void testOneAskTakesTwoEqualSizeBids() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newBid(10, 13));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newBid(12, 7));
    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newAsk(8, 13 + 7));
    assertTrue(RESULT.getTakeSize()      == 13 + 7);
    assertTrue(RESULT.getTakeValue()     == (10 * 13) + (12 * 7));
    assertTrue(RESULT.getMakers().size() == 2);
  }

  @Test
  public void testOneAskTakesTwoLargerSizeBids() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newBid(10, 15));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newBid(12, 21));
    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newAsk(8, 23));
    assertTrue(RESULT.getTakeSize()      == 23);
    assertTrue(RESULT.getTakeValue()     == (12 * 21) + (10 * 2));
    assertTrue(RESULT.getMakers().size() == 2);
  }

  @Test
  public void testOneBidTakesTwoSmallerSizeAsks() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newAsk(10, 5));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newAsk(12, 4));
    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newBid(13, 20));
    assertTrue(RESULT.getTakeSize()      == 5 + 4);
    assertTrue(RESULT.getTakeValue()     == (10 * 5) + (12 * 4));
    assertTrue(RESULT.getMakers().size() == 2);
  }

  @Test
  public void testOneBidTakesTwoEqualSizeAsks() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newAsk(10, 32));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newAsk(12, 64));
    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newBid(12, 32 + 64));
    assertTrue(RESULT.getTakeSize()      == 32 + 64);
    assertTrue(RESULT.getTakeValue()     == (10 * 32) + (12 * 64));
    assertTrue(RESULT.getMakers().size() == 2);
  }

  @Test
  public void testOneBidTakesTwoLargerSizeAsks() {
    final LimitOrderBook BOOK   = new LimitOrderBook(10);
          TakeResult     RESULT = BOOK.add(newAsk(10, 31));

    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newAsk(12, 33));
    assertTrue(RESULT.getTakeSize()  == 0);
    assertTrue(RESULT.getTakeValue() == 0);
    assertTrue(RESULT.getMakers().isEmpty());

    RESULT = BOOK.add(newBid(12, 34));
    assertTrue(RESULT.getTakeSize()      == 34);
    assertTrue(RESULT.getTakeValue()     == (10 * 31) + (12 * 3));
    assertTrue(RESULT.getMakers().size() == 2);
  }

}
