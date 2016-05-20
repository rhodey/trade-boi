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

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;

public class LimitQueue {

  private final Map<Double, Limit> map = new HashMap<>();
  private final Queue<Limit> queue;

  public LimitQueue(Order.Side side) {
    if (side.equals(Order.Side.ASK)) {
      queue = new PriorityQueue<>(new AskSorter());
    } else {
      queue = new PriorityQueue<>(new BidSorter());
    }
  }

  public Optional<Limit> peek() {
    return Optional.ofNullable(queue.peek());
  }

  public void addOrder(Order order) {
    Limit limit = map.get(order.getPrice());

    if (limit == null) {
      limit = new Limit(order.getPrice());
      map.put(order.getPrice(), limit);
      queue.add(limit);
    }

    limit.add(order);
  }

  public Optional<Order> removeOrder(Double price, String orderId) {
    Optional<Order> order = Optional.empty();
    Optional<Limit> limit = Optional.ofNullable(map.get(price));

    if (limit.isPresent()) {
      order = limit.get().remove(orderId);
      if (order.isPresent() && !limit.get().peek().isPresent()) {
        map.remove(price);
        if (!queue.remove(limit.get())) { throw new IllegalStateException("f12"); }
      }
    }

    return order;
  }

  public Optional<Order> reduceOrder(Double price, String orderId, double size) {
    Optional<Order> order = Optional.empty();
    Optional<Limit> limit = Optional.ofNullable(map.get(price));

    if (limit.isPresent()) {
      order = limit.get().reduce(orderId, size);
      if (order.isPresent() && !limit.get().peek().isPresent()) {
        map.remove(price);
        if (!queue.remove(limit.get())) { throw new IllegalStateException("f12"); }
      }
    }

    return order;
  }

  private boolean isTaken(Limit maker, Order taker) {
    if (taker instanceof MarketOrder) {
      return ((MarketOrder) taker).getSizeRemainingFor(maker.getPrice()) > 0;
    } else if (taker.getSide().equals(Order.Side.BID)) {
      return taker.getPrice() >= maker.getPrice();
    } else {
      return taker.getPrice() <= maker.getPrice();
    }
  }

  public List<Order> takeLiquidityFromBestLimit(Order taker) {
    Optional<Limit> maker = peek();
    if (maker.isPresent() && isTaken(maker.get(), taker)) {
      List<Order> makers = maker.get().takeLiquidity(taker);

      if (!maker.get().peek().isPresent()) {
        map.remove(maker.get().getPrice());
        if (!queue.remove().equals(maker.get())) { throw new IllegalStateException("f12"); }
      }

      return makers;
    } else {
      return new LinkedList<>();
    }
  }

  public void clear() {
    map.clear();
    while (!queue.isEmpty()) { queue.remove().clear(); }
  }

  private static class AskSorter implements Comparator<Limit> {
    @Override
    public int compare(Limit ask1, Limit ask2) {
      if (ask1.getPrice() < ask2.getPrice()) {
        return -1;
      } else if (ask1.getPrice() == ask2.getPrice()) {
        return 0;
      } else {
        return 1;
      }
    }
  }

  private static class BidSorter implements Comparator<Limit> {
    @Override
    public int compare(Limit bid1, Limit bid2) {
      if (bid1.getPrice() > bid2.getPrice()) {
        return -1;
      } else if (bid1.getPrice() == bid2.getPrice()) {
        return 0;
      } else {
        return 1;
      }
    }
  }

}
