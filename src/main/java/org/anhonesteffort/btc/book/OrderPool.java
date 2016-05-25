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
  private final Map<Long, Order>   taken;

  public OrderPool(int initLimitCapacity, int initMarketCapacity) {
    limitOrders  = new ArrayDeque<>(initLimitCapacity);
    marketOrders = new ArrayDeque<>(initMarketCapacity);
    taken        = new HashMap<>(initLimitCapacity + initMarketCapacity);

    Long serial = 0l;
    for (int i = 0; i < initLimitCapacity; i++) {
      limitOrders.add(new Order(serial++, null, null, -1l, -1l));
    }
    for (int i = 0; i < initMarketCapacity; i++) {
      marketOrders.add(new MarketOrder(serial++, null, null, -1l, -1l));
    }
  }

  public Order take(String orderId, Order.Side side, long price, long size) {
    Order take = limitOrders.remove();
    take.init(orderId, side, price, size);
    taken.put(take.serial, take);
    return take;
  }

  public MarketOrder takeMarket(String orderId, Order.Side side, long size, long funds) {
    MarketOrder take = marketOrders.remove();
    take.initMarket(orderId, side, size, funds);
    taken.put(take.serial, take);
    return take;
  }

  public void returnOrder(Order order) {
    if (order instanceof MarketOrder) {
      marketOrders.add((MarketOrder) order);
    } else {
      limitOrders.add(order);
    }
    taken.remove(order.serial);
  }

  // todo: just clear and re-allocate new objects, this only happens on error
  public void returnAll() {
    taken.keySet().stream().map(taken::get).forEach(order -> {
      if (order instanceof MarketOrder) {
        marketOrders.add((MarketOrder) order);
      } else {
        limitOrders.add(order);
      }
    });
    taken.clear();
  }

}
