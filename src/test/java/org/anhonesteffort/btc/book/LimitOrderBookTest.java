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

public class LimitOrderBookTest extends BaseTest {

  @Test
  public void testAddRemoveAsk() {
    final LimitOrderBook BOOK = new LimitOrderBook();

    BOOK.add(newAsk("00", 10, 20));

    assert BOOK.remove(Order.Side.ASK, 10d, "00").isPresent();
    assert !BOOK.remove(Order.Side.ASK, 10d, "00").isPresent();
  }

  @Test
  public void testAddRemoveBid() {
    final LimitOrderBook BOOK = new LimitOrderBook();

    BOOK.add(newBid("00", 10, 20));

    assert BOOK.remove(Order.Side.BID, 10d, "00").isPresent();
    assert !BOOK.remove(Order.Side.BID, 10d, "00").isPresent();
  }

  @Test
  public void testAskTakesEmptyBook() {
    final LimitOrderBook BOOK   = new LimitOrderBook();
    final TakeResult     RESULT = BOOK.add(newAsk(10, 10));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();
  }

  @Test
  public void testBidTakesEmptyBook() {
    final LimitOrderBook BOOK   = new LimitOrderBook();
    final TakeResult     RESULT = BOOK.add(newBid(10, 10));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();
  }

  @Test
  public void testAskWontTakeSmallerBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook();
          TakeResult     RESULT = BOOK.add(newBid(8, 10));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();

    RESULT = BOOK.add(newAsk(9, 10));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();
  }

  @Test
  public void testBidWontTakeLargerAsk() {
    final LimitOrderBook BOOK   = new LimitOrderBook();
          TakeResult     RESULT = BOOK.add(newAsk(8, 10));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();

    RESULT = BOOK.add(newBid(7, 10));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();
  }

  @Test
  public void testOneAskTakesOneSmallerSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook();
          TakeResult     RESULT = BOOK.add(newBid(10, 5));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();

    RESULT = BOOK.add(newAsk(10, 20));
    assert RESULT.getTakeSize()      == 5;
    assert RESULT.getTakeValue()     == 10 * 5;
    assert RESULT.getMakers().size() == 1;
  }

  @Test
  public void testOneAskTakesOneEqualSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook();
          TakeResult     RESULT = BOOK.add(newBid(10, 5));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();

    RESULT = BOOK.add(newAsk(10, 5));
    assert RESULT.getTakeSize()      == 5;
    assert RESULT.getTakeValue()     == 10 * 5;
    assert RESULT.getMakers().size() == 1;
  }

  @Test
  public void testOneAskTakesOneLargerSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook();
          TakeResult     RESULT = BOOK.add(newBid(10, 15));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();

    RESULT = BOOK.add(newAsk(10, 5));
    assert RESULT.getTakeSize()      == 5;
    assert RESULT.getTakeValue()     == 10 * 5;
    assert RESULT.getMakers().size() == 1;
  }

  @Test
  public void testOneBidTakesOneSmallerSizeAsk() {
    final LimitOrderBook BOOK   = new LimitOrderBook();
          TakeResult     RESULT = BOOK.add(newAsk(10, 5));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();

    RESULT = BOOK.add(newBid(10, 8));
    assert RESULT.getTakeSize()      == 5;
    assert RESULT.getTakeValue()     == 10 * 5;
    assert RESULT.getMakers().size() == 1;
  }

  @Test
  public void testOneBidTakesOneEqualSizeAsk() {
    final LimitOrderBook BOOK   = new LimitOrderBook();
          TakeResult     RESULT = BOOK.add(newAsk(10, 5));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();

    RESULT = BOOK.add(newBid(10, 5));
    assert RESULT.getTakeSize()      == 5;
    assert RESULT.getTakeValue()     == 10 * 5;
    assert RESULT.getMakers().size() == 1;
  }

  @Test
  public void testOneBidTakesOneLargerSizeAsk() {
    final LimitOrderBook BOOK   = new LimitOrderBook();
          TakeResult     RESULT = BOOK.add(newAsk(10, 5));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();

    RESULT = BOOK.add(newBid(10, 2));
    assert RESULT.getTakeSize()      == 2;
    assert RESULT.getTakeValue()     == 10 * 2;
    assert RESULT.getMakers().size() == 1;
  }

  @Test
  public void testTwoAsksTakesOneSmallerSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook();
          TakeResult     RESULT = BOOK.add(newBid(10, 20));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();

    RESULT = BOOK.add(newAsk(10, 5));
    assert RESULT.getTakeSize()      == 5;
    assert RESULT.getTakeValue()     == 10 * 5;
    assert RESULT.getMakers().size() == 1;
    RESULT.clearMakerValueRemoved();

    RESULT = BOOK.add(newAsk(10, 25));
    assert RESULT.getTakeSize()      == 15;
    assert RESULT.getTakeValue()     == 10 * 15;
    assert RESULT.getMakers().size() == 1;
  }

  @Test
  public void testTwoAsksTakesOneEqualSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook();
          TakeResult     RESULT = BOOK.add(newBid(10, 20));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();

    RESULT = BOOK.add(newAsk(10, 12));
    assert RESULT.getTakeSize()      == 12;
    assert RESULT.getTakeValue()     == 10 * 12;
    assert RESULT.getMakers().size() == 1;
    RESULT.clearMakerValueRemoved();

    RESULT = BOOK.add(newAsk(10, 8));
    assert RESULT.getTakeSize()      == 8;
    assert RESULT.getTakeValue()     == 10 * 8;
    assert RESULT.getMakers().size() == 1;
  }

  @Test
  public void testTwoAsksTakesOneLargerSizeBid() {
    final LimitOrderBook BOOK   = new LimitOrderBook();
          TakeResult     RESULT = BOOK.add(newBid(10, 30));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();

    RESULT = BOOK.add(newAsk(10, 12));
    assert RESULT.getTakeSize()      == 12;
    assert RESULT.getTakeValue()     == 10 * 12;
    assert RESULT.getMakers().size() == 1;
    RESULT.clearMakerValueRemoved();

    RESULT = BOOK.add(newAsk(10, 8));
    assert RESULT.getTakeSize()      == 8;
    assert RESULT.getTakeValue()     == 10 * 8;
    assert RESULT.getMakers().size() == 1;
  }

  @Test
  public void testTwoBidsTakesOneSmallerSizeAsk() {
    final LimitOrderBook BOOK   = new LimitOrderBook();
          TakeResult     RESULT = BOOK.add(newAsk(10, 20));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();

    RESULT = BOOK.add(newBid(10, 5));
    assert RESULT.getTakeSize()      == 5;
    assert RESULT.getTakeValue()     == 10 * 5;
    assert RESULT.getMakers().size() == 1;
    RESULT.clearMakerValueRemoved();

    RESULT = BOOK.add(newBid(10, 25));
    assert RESULT.getTakeSize()      == 15;
    assert RESULT.getTakeValue()     == 10 * 15;
    assert RESULT.getMakers().size() == 1;
  }

  @Test
  public void testTwoBidsTakesOneEqualSizeAsk() {
    final LimitOrderBook BOOK   = new LimitOrderBook();
          TakeResult     RESULT = BOOK.add(newAsk(10, 20));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();

    RESULT = BOOK.add(newBid(10, 9));
    assert RESULT.getTakeSize()      == 9;
    assert RESULT.getTakeValue()     == 10 * 9;
    assert RESULT.getMakers().size() == 1;
    RESULT.clearMakerValueRemoved();

    RESULT = BOOK.add(newBid(10, 11));
    assert RESULT.getTakeSize()      == 11;
    assert RESULT.getTakeValue()     == 10 * 11;
    assert RESULT.getMakers().size() == 1;
  }

  @Test
  public void testTwoBidsTakesOneLargerSizeAsk() {
    final LimitOrderBook BOOK   = new LimitOrderBook();
          TakeResult     RESULT = BOOK.add(newAsk(10, 30));

    assert RESULT.getTakeSize()  == 0;
    assert RESULT.getTakeValue() == 0;
    assert RESULT.getMakers().isEmpty();

    RESULT = BOOK.add(newBid(10, 5));
    assert RESULT.getTakeSize()      == 5;
    assert RESULT.getTakeValue()     == 10 * 5;
    assert RESULT.getMakers().size() == 1;
    RESULT.clearMakerValueRemoved();

    RESULT = BOOK.add(newBid(10, 6));
    assert RESULT.getTakeSize()      == 6;
    assert RESULT.getTakeValue()     == 10 * 6;
    assert RESULT.getMakers().size() == 1;
  }

}
