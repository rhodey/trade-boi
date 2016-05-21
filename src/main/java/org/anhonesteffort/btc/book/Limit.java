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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

public class Limit {

  private final Queue<Order>       orderQueue = new ArrayDeque<>(12); // todo: size from constructor
  private final Map<String, Order> orderMap   = new HashMap<>();

  private final long price;
  private       long volume;

  public Limit(long price) {
    this.price  = price;
    this.volume = 0;
  }

  public long getPrice() {
    return price;
  }

  public long getVolume() {
    return volume;
  }

  public Optional<Order> peek() {
    return Optional.ofNullable(orderQueue.peek());
  }

  public void add(Order order) {
    orderMap.put(order.getOrderId(), order);
    orderQueue.add(order);
    volume += order.getSizeRemaining();
  }

  public Optional<Order> remove(String orderId) {
    Optional<Order> order = Optional.ofNullable(orderMap.remove(orderId));
    if (order.isPresent()) {
      orderQueue.remove(order.get());
      volume -= order.get().getSizeRemaining();
    }
    return order;
  }

  public Optional<Order> reduce(String orderId, long size) {
    Optional<Order> order = Optional.ofNullable(orderMap.get(orderId));
    if (order.isPresent()) {
      order.get().subtract(size, price);
      volume -= size;
      if (order.get().getSizeRemaining() <= 0) {
        orderMap.remove(orderId);
        orderQueue.remove(order.get());
      }
    }
    return order;
  }

  private long getTakeSize(Order taker) {
    if (taker instanceof MarketOrder) {
      return ((MarketOrder) taker).getSizeRemainingFor(price);
    } else {
      return taker.getSizeRemaining();
    }
  }

  private Optional<Order> takeLiquidityFromNextMaker(Order taker) {
    Optional<Order> maker = Optional.ofNullable(orderQueue.peek());
    if (maker.isPresent()) {
      long volumeRemoved = maker.get().takeSize(getTakeSize(taker));

      if (maker.get().getSizeRemaining() <= 0) {
        orderMap.remove(maker.get().getOrderId());
        orderQueue.remove();
      }

      volume -= volumeRemoved;
      taker.subtract(volumeRemoved, maker.get().getPrice());
    }
    return maker;
  }

  public List<Order> takeLiquidity(Order taker) {
    List<Order>     makers = new LinkedList<>();
    Optional<Order> maker  = null;

    while (getTakeSize(taker) > 0) {
      maker = takeLiquidityFromNextMaker(taker);
      if (maker.isPresent()) {
        makers.add(maker.get());
      } else {
        break;
      }
    }

    return makers;
  }

  public void clear() {
    orderQueue.clear();
    orderMap.clear();
    volume = 0;
  }

}
