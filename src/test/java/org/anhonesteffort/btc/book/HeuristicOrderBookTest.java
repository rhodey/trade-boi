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

public class HeuristicOrderBookTest {

  private Integer nextOrderId = 0;

  private Order newAsk(double price, double size) {
    return new Order((nextOrderId++).toString(), Order.Side.ASK, price, size);
  }

  private Order newBid(double price, double size) {
    return new Order((nextOrderId++).toString(), Order.Side.BID, price, size);
  }

  @Test
  public void testEmptyBookSpread() {
    final HeuristicLimitOrderBook BOOK = new HeuristicLimitOrderBook();
    assert !BOOK.getSpread().isPresent();
  }

  @Test
  public void testSpread() {
    final HeuristicLimitOrderBook BOOK = new HeuristicLimitOrderBook();

    BOOK.add(newAsk(10, 20));
    assert !BOOK.getSpread().isPresent();

    BOOK.add(newBid(8, 20));
    assert BOOK.getSpread().isPresent();
    assert BOOK.getSpread().get() == 2;

    BOOK.add(newBid(9, 20));
    assert BOOK.getSpread().isPresent();
    assert BOOK.getSpread().get() == 1;
  }

}
