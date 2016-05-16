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
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;

public class LimitOrderBook {

  private final Queue<Limit>       askQueue = new PriorityQueue<>(new AskSorter());
  private final Queue<Limit>       bidQueue = new PriorityQueue<>(new BidSorter());
  private final Map<Double, Limit> askMap   = new HashMap<>();
  private final Map<Double, Limit> bidMap   = new HashMap<>();

  private void addAsk(Order order) {
    Limit limit = askMap.get(order.getPrice());

    if (limit == null) {
      limit = new Limit(order.getPrice());
      askMap.put(order.getPrice(), limit);
      askQueue.add(limit);
    }

    limit.add(order);
  }

  private void addBid(Order order) {
    Limit limit = bidMap.get(order.getPrice());

    if (limit == null) {
      limit = new Limit(order.getPrice());
      bidMap.put(order.getPrice(), limit);
      bidQueue.add(limit);
    }

    limit.add(order);
  }

  public void add(Order order) {
    if (order.getSide().equals(Order.Side.ASK)) {
      addAsk(order);
    } else {
      addBid(order);
    }
  }

  public Optional<Double> getSpread() {
    Optional<Limit> ask = Optional.ofNullable(askQueue.peek());
    Optional<Limit> bid = Optional.ofNullable(bidQueue.peek());

    if (ask.isPresent() && bid.isPresent()) {
      return Optional.of(ask.get().getPrice() - bid.get().getPrice());
    } else {
      return Optional.empty();
    }
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
