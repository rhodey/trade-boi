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

public class LimitOrderBookTest {

  private Integer nextOrderId = 0;

  private Order newAsk(double price, double size) {
    return new Order((nextOrderId++).toString(), Order.Side.ASK, price, size);
  }

  private Order newBid(double price, double size) {
    return new Order((nextOrderId++).toString(), Order.Side.BID, price, size);
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
  public void testAskTakesOneSmallerSizeBid() {
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
  public void testAskTakesOneEqualSizeBid() {
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
  public void testAskTakesOneLargerSizeBid() {
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
  public void testBidTakesOneSmallerSizeAsk() {
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
  public void testBidTakesOneEqualSizeAsk() {
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
  public void testBidTakesOneLargerSizeAsk() {
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

}
