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

public class LimitTest {

  private Order newOrder(String orderId, double size) {
    return new Order(orderId, Order.Side.BUY, 10.20, size);
  }

  @Test
  public void testAddRemoveVolume() {
    final Limit LIMIT = new Limit(10.20);

    assert LIMIT.getPrice()  == 10.20;
    assert LIMIT.getVolume() == 0;

    LIMIT.add(newOrder("00", 10));
    assert LIMIT.getVolume() == 10;
    LIMIT.add(newOrder("01", 20));
    assert LIMIT.getVolume() == 30;

    LIMIT.remove("00");
    assert LIMIT.getVolume() == 20;
    LIMIT.remove("01");
    assert LIMIT.getVolume() == 0;
  }

  @Test
  public void testFillVolumeEmptyLimit() {
    final Limit            LIMIT = new Limit(10.20);
    final Limit.FillResult FILL1 = LIMIT.fillVolume(10);

    assert FILL1.getVolume() == 0;
    assert FILL1.getFills().size() == 0;
  }

  @Test
  public void testFillVolumeOneFill() {
    final Limit LIMIT = new Limit(10.20);

    LIMIT.add(newOrder("00", 10));

    final Limit.FillResult FILL1 = LIMIT.fillVolume(10);

    assert FILL1.getVolume()       == 10;
    assert FILL1.getFills().size() == 1;
    assert LIMIT.getVolume()       == 0;
  }

  @Test
  public void testFillVolumeOnePartialFill() {
    final Limit LIMIT = new Limit(10.20);

    LIMIT.add(newOrder("00", 10));

    final Limit.FillResult FILL1 = LIMIT.fillVolume(8);

    assert FILL1.getVolume()       == 8;
    assert FILL1.getFills().size() == 1;
    assert LIMIT.getVolume()       == 2;
  }

}
