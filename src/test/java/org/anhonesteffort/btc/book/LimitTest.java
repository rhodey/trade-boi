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

import java.util.List;

public class LimitTest extends BaseTest {

  private Order newOrder(String orderId, double size) {
    return newBid(orderId, 10.20d, size);
  }

  private MarketOrder newMarketOrder(String orderId, double size, double funds) {
    return newMarketBid(orderId, size, funds);
  }

  @Test
  public void testGettersAndAddRemoveClearVolume() {
    final Limit LIMIT = new Limit(10.20);

    assert LIMIT.getPrice()  == 10.20;
    assert LIMIT.getVolume() == 0;

    LIMIT.add(newOrder("00", 10));
    assert LIMIT.getVolume() == 10;

    LIMIT.add(newOrder("01", 20));
    assert LIMIT.getVolume() == 30;

    LIMIT.remove("00");
    assert LIMIT.getVolume() == 20;

    LIMIT.clear();
    assert !LIMIT.remove("01").isPresent();
    assert LIMIT.getVolume() == 0;
  }

  @Test
  public void testTakerWithNoMaker() {
    final Limit       LIMIT   = new Limit(10.20);
    final Order       TAKER1  = newOrder("00", 10);
    final List<Order> MAKERS1 = LIMIT.takeLiquidity(TAKER1);

    assert TAKER1.getSizeRemaining() == 10;
    assert MAKERS1.size()            ==  0;
  }

  @Test
  public void testMarketTakerWithNoMaker() {
    final Limit       LIMIT   = new Limit(10.20);
    final MarketOrder TAKER1  = newMarketOrder("00", 10, 20);
    final List<Order> MAKERS1 = LIMIT.takeLiquidity(TAKER1);

    assert TAKER1.getVolumeRemoved() == 0;
    assert MAKERS1.size()            == 0;
  }

  @Test
  public void testOneFullMakeOneFullTake() {
    final Limit LIMIT = new Limit(10.20);

    LIMIT.add(newOrder("00", 10));

    final Order       TAKER1  = newOrder("01", 10);
    final List<Order> MAKERS1 = LIMIT.takeLiquidity(TAKER1);

    assert TAKER1.getSizeRemaining()         == 0;
    assert MAKERS1.size()                    == 1;
    assert MAKERS1.get(0).getSizeRemaining() == 0;
    assert LIMIT.getVolume()                 == 0;
  }

  @Test
  public void testPartialTake() {
    final Limit LIMIT = new Limit(10.20);

    LIMIT.add(newOrder("00", 10));

    final Order       TAKER1  = newOrder("01", 8);
    final List<Order> MAKERS1 = LIMIT.takeLiquidity(TAKER1);

    assert TAKER1.getSizeRemaining()         == 0;
    assert MAKERS1.size()                    == 1;
    assert MAKERS1.get(0).getSizeRemaining() == 2;
    assert LIMIT.getVolume()                 == 2;
  }

  @Test
  public void testOneFullTakeOnePartialTake() {
    final Limit LIMIT = new Limit(10.20);

    LIMIT.add(newOrder("00", 10));

    final Order       TAKER1  = newOrder("01", 8);
    final List<Order> MAKERS1 = LIMIT.takeLiquidity(TAKER1);

    assert TAKER1.getSizeRemaining()         == 0;
    assert MAKERS1.size()                    == 1;
    assert MAKERS1.get(0).getSizeRemaining() == 2;
    assert LIMIT.getVolume()                 == 2;

    final Order       TAKER2  = newOrder("02", 4);
    final List<Order> MAKERS2 = LIMIT.takeLiquidity(TAKER2);

    assert TAKER2.getSizeRemaining()         == 2;
    assert MAKERS2.size()                    == 1;
    assert MAKERS2.get(0).getSizeRemaining() == 0;
    assert LIMIT.getVolume()                 == 0;
  }

  @Test
  public void testTwoFullMakesOneFullTake() {
    final Limit LIMIT = new Limit(10.20);

    LIMIT.add(newOrder("00", 10));
    LIMIT.add(newOrder("01", 30));

    final Order       TAKER1  = newOrder("02", 40);
    final List<Order> MAKERS1 = LIMIT.takeLiquidity(TAKER1);

    assert TAKER1.getSizeRemaining()         == 0;
    assert MAKERS1.size()                    == 2;
    assert MAKERS1.get(0).getSizeRemaining() == 0;
    assert MAKERS1.get(1).getSizeRemaining() == 0;
    assert LIMIT.getVolume()                 == 0;
  }

  @Test
  public void testOneFullMakeOnePartialMakeOneFullTake() {
    final Limit LIMIT = new Limit(10.20);

    LIMIT.add(newOrder("00", 10));
    LIMIT.add(newOrder("01", 30));

    final Order       TAKER1  = newOrder("02", 30);
    final List<Order> MAKERS1 = LIMIT.takeLiquidity(TAKER1);

    assert TAKER1.getSizeRemaining()         ==  0;
    assert MAKERS1.size()                    ==  2;
    assert MAKERS1.get(0).getSizeRemaining() ==  0;
    assert MAKERS1.get(1).getSizeRemaining() == 10;
    assert LIMIT.getVolume()                 == 10;
  }

}
