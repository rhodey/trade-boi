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

public class OrderPoolTest {

  @Test
  public void testTakeReturn() {
    final OrderPool POOL   = new OrderPool(2, 1);
    final Order     ORDER0 = POOL.take("00", Order.Side.ASK, 10, 20);

    assert ORDER0.getOrderId().equals("00");
    assert ORDER0.getSide().equals(Order.Side.ASK);
    assert ORDER0.getPrice() == 10;
    assert ORDER0.getSize()  == 20;

    POOL.returnOrder(ORDER0);
    POOL.take("01", Order.Side.BID, 30, 40);
    assert POOL.take("02", Order.Side.BID, 50, 60).equals(ORDER0);
  }

}
