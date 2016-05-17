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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

public class Limit {

  private final Queue<Order>       orderQueue = new LinkedList<>();
  private final Map<String, Order> orderMap   = new HashMap<>();

  private final double price;
  private       double volume;

  public Limit(double price) {
    this.price  = price;
    this.volume = 0;
  }

  public double getPrice() {
    return price;
  }

  public double getVolume() {
    return volume;
  }

  public void add(Order order) {
    orderMap.put(order.getOrderId(), order);
    orderQueue.add(order);
    volume += order.getRemaining();
  }

  public Optional<Order> remove(String orderId) {
    Optional<Order> order = Optional.ofNullable(orderMap.remove(orderId));
    if (order.isPresent()) {
      orderQueue.remove(order.get());
      volume -= order.get().getRemaining();
    }
    return order;
  }

  private Optional<Order> takeLiquidityFromNextMaker(Order taker) {
    Optional<Order> maker = Optional.ofNullable(orderQueue.peek());
    if (maker.isPresent()) {
      double volumeRemoved = maker.get().takeSize(taker.getRemaining());

      if (maker.get().getRemaining() <= 0) {
        orderMap.remove(maker.get().getOrderId());
        orderQueue.remove();
      }

      volume -= volumeRemoved;
      taker.takeSize(volumeRemoved);
    }
    return maker;
  }

  public List<Order> takeLiquidity(Order taker) {
    List<Order>     makers = new LinkedList<>();
    Optional<Order> maker  = null;

    while (taker.getRemaining() > 0) {
      maker = takeLiquidityFromNextMaker(taker);
      if (maker.isPresent()) {
        makers.add(maker.get());
      } else {
        break;
      }
    }

    return makers;
  }

}
