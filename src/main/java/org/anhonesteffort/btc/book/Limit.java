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

  private VolumeResult removeVolume(double target) {
    Optional<Order> next = Optional.ofNullable(orderQueue.peek());

    if (!next.isPresent()) {
      return new VolumeResult(next, 0);
    } else {
      double removed = next.get().takeSize(target);

      if (next.get().getRemaining() <= 0) {
        orderMap.remove(next.get().getOrderId());
        orderQueue.remove();
      }

      volume -= removed;
      return new VolumeResult(next, removed);
    }
  }

  public FillResult fillVolume(double targetVolume) {
    Queue<Order> fills        = new LinkedList<>();
    double       filledVolume = 0;
    VolumeResult result       = null;

    while (targetVolume > 0) {
      result        = removeVolume(targetVolume);
      targetVolume -= result.volume;
      filledVolume += result.volume;

      if (result.order.isPresent()) {
        fills.add(result.order.get());
      } else {
        break;
      }
    }

    return new FillResult(fills, filledVolume);
  }

  private static class VolumeResult {
    private final Optional<Order> order;
    private final double volume;

    public VolumeResult(Optional<Order> order, double volume) {
      this.order  = order;
      this.volume = volume;
    }
  }

  public static class FillResult {
    private final Queue<Order> fills;
    private final double volume;

    public FillResult(Queue<Order> fills, double volume) {
      this.fills  = fills;
      this.volume = volume;
    }

    public Queue<Order> getFills() {
      return fills;
    }

    public double getVolume() {
      return volume;
    }
  }

}
