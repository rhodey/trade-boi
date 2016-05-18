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

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class OrderPool {

  private final Queue<Order>       limitOrders;
  private final Queue<MarketOrder> marketOrders;
  private final Map<Long, Order>   removed;

  public OrderPool(int initLimitCapacity, int initMarketCapacity) {
    limitOrders  = new ArrayDeque<>(initLimitCapacity);
    marketOrders = new ArrayDeque<>(initMarketCapacity);
    removed      = new HashMap<>(initLimitCapacity + initMarketCapacity);

    long serial = 0;
    for (int i = 0; i < initLimitCapacity; i++) {
      limitOrders.add(new Order(serial++, null, null, -1, -1));
    }
    for (int i = 0; i < initMarketCapacity; i++) {
      marketOrders.add(new MarketOrder(serial++, null, null, -1, -1));
    }
  }

  public Order take(String orderId, Order.Side side, double price, double size) {
    Order taken = limitOrders.remove();
    taken.init(orderId, side, price, size);
    removed.put(taken.serial, taken);
    return taken;
  }

  public MarketOrder takeMarket(String orderId, Order.Side side, double size, double funds) {
    MarketOrder taken = marketOrders.remove();
    taken.initMarket(orderId, side, size, funds);
    removed.put(taken.serial, taken);
    return taken;
  }

  public void returnOrder(Order order) {
    if (order instanceof MarketOrder) {
      marketOrders.add((MarketOrder) order);
    } else {
      limitOrders.add(order);
    }
    removed.remove(order.serial);
  }

  public void returnAll() {
    removed.keySet().stream().map(removed::get).forEach(order -> {
      if (order instanceof MarketOrder) {
        marketOrders.add((MarketOrder) order);
      } else {
        limitOrders.add(order);
      }
    });
    removed.clear();
  }

}
