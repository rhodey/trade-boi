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

public class MarketOrderTest {

  @Test
  public void testWithSize() {
    final MarketOrder ORDER = new MarketOrder("lol", Order.Side.BID, 100, -1);

    assertTrue(ORDER.getOrderId().equals("lol"));
    assertTrue(ORDER.getSide().equals(Order.Side.BID));
    assertTrue(ORDER.getPrice()         ==   0);
    assertTrue(ORDER.getSize()          == 100);
    assertTrue(ORDER.getSizeRemaining() == 100);
    assertTrue(ORDER.getValueRemoved()  ==   0);

    assertTrue(ORDER.getFunds()                 <   0);
    assertTrue(ORDER.getFundsRemaining()        <   0);
    assertTrue(ORDER.getVolumeRemoved()        ==   0);
    assertTrue(ORDER.getSizeRemainingFor(1337) == 100);

    ORDER.subtract(75, 1337);

    assertTrue(ORDER.getVolumeRemoved()        == 75);
    assertTrue(ORDER.getSizeRemainingFor(1337) == 25);

    ORDER.subtract(25, 31337);

    assertTrue(ORDER.getVolumeRemoved()         == 100);
    assertTrue(ORDER.getSizeRemainingFor(31337) ==   0);
    assertTrue(ORDER.getSizeRemaining()         ==   0);
    assertTrue(ORDER.getValueRemoved()          ==   0);
  }

  @Test
  public void testWithFunds() {
    final MarketOrder ORDER = new MarketOrder("lol", Order.Side.BID, -1, 100);

    assertTrue(ORDER.getOrderId().equals("lol"));
    assertTrue(ORDER.getSide().equals(Order.Side.BID));
    assertTrue(ORDER.getPrice()         == 0);
    assertTrue(ORDER.getSize()           < 0);
    assertTrue(ORDER.getSizeRemaining()  < 0);
    assertTrue(ORDER.getValueRemoved()  == 0);

    assertTrue(ORDER.getFunds()              == 100);
    assertTrue(ORDER.getFundsRemaining()     == 100);
    assertTrue(ORDER.getVolumeRemoved()      ==   0);
    assertTrue(ORDER.getSizeRemainingFor(25) ==   4);

    ORDER.subtract(3, 25);

    assertTrue(ORDER.getVolumeRemoved()      == 3);
    assertTrue(ORDER.getSizeRemainingFor(25) == 1);

    ORDER.subtract(1, 25);

    assertTrue(ORDER.getVolumeRemoved()     ==  4);
    assertTrue(ORDER.getSizeRemainingFor(1) ==  0);
    assertTrue(ORDER.getSizeRemaining()      <  0);
    assertTrue(ORDER.getValueRemoved()      ==  0);
  }

  @Test
  public void testWithSizeAndFunds() {
    final MarketOrder ORDER = new MarketOrder("lol", Order.Side.BID, 100, 50);

    assertTrue(ORDER.getOrderId().equals("lol"));
    assertTrue(ORDER.getSide().equals(Order.Side.BID));
    assertTrue(ORDER.getPrice()         ==   0);
    assertTrue(ORDER.getSize()          == 100);
    assertTrue(ORDER.getSizeRemaining() == 100);
    assertTrue(ORDER.getValueRemoved()  ==   0);

    assertTrue(ORDER.getFunds()             ==  50);
    assertTrue(ORDER.getFundsRemaining()    ==  50);
    assertTrue(ORDER.getVolumeRemoved()     ==   0);
    assertTrue(ORDER.getSizeRemainingFor(1) ==  50);
    assertTrue(ORDER.getSizeRemainingFor(5) == (50 / 5));

    ORDER.subtract(25, 1);

    assertTrue(ORDER.getVolumeRemoved()     == 25);
    assertTrue(ORDER.getSizeRemainingFor(5) == (25 / 5));

    ORDER.subtract(10, 2);

    assertTrue(ORDER.getVolumeRemoved()     == 35);
    assertTrue(ORDER.getSizeRemainingFor(1) ==  5);
    assertTrue(ORDER.getSizeRemaining()     == 100 - (25 + 10));
    assertTrue(ORDER.getValueRemoved()      ==  0);

    ORDER.subtract(5, 1);

    assertTrue(ORDER.getVolumeRemoved()     == 40);
    assertTrue(ORDER.getSizeRemainingFor(1) ==  0);
    assertTrue(ORDER.getSizeRemaining()     ==  100 - (25 + 10 + 5));
    assertTrue(ORDER.getValueRemoved()      ==  0);
  }

}
