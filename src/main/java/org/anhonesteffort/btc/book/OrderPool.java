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
import java.util.Queue;
import java.util.stream.IntStream;

public class OrderPool {

  private final Queue<Order>       limitOrders;
  private final Queue<MarketOrder> marketOrders;

  public OrderPool(int limitOrderCapacity, int marketOrderCapacity) {
    limitOrders  = new ArrayDeque<>(limitOrderCapacity);
    marketOrders = new ArrayDeque<>(marketOrderCapacity);

    IntStream.range(0, limitOrderCapacity).forEach(i -> limitOrders.add(new Order(null, null, -1, -1)));
    IntStream.range(0, marketOrderCapacity).forEach(i -> marketOrders.add(new MarketOrder(null, null, -1, -1)));
  }

  public Order take(String orderId, Order.Side side, double price, double size) {
    Order taken = limitOrders.remove();
    taken.init(orderId, side, price, size);
    return taken;
  }

  public MarketOrder takeMarket(String orderId, Order.Side side, double size, double funds) {
    MarketOrder taken = marketOrders.remove();
    taken.initMarket(orderId, side, size, funds);
    return taken;
  }

  public void returnOrder(Order order) {
    if (order instanceof MarketOrder) {
      marketOrders.add((MarketOrder) order);
    } else {
      limitOrders.add(order);
    }
  }

}
