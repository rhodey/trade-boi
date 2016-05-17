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

public class LimitOrderBook {

  private final Queue<Limit>       askLimitQueue = new PriorityQueue<>(new AskSorter());
  private final Queue<Limit>       bidLimitQueue = new PriorityQueue<>(new BidSorter());
  private final Map<Double, Limit> askLimitMap   = new HashMap<>();
  private final Map<Double, Limit> bidLimitMap   = new HashMap<>();

  private void addAsk(Order ask) {
    Limit limit = askLimitMap.get(ask.getPrice());

    if (limit == null) {
      limit = new Limit(ask.getPrice());
      askLimitMap.put(ask.getPrice(), limit);
      askLimitQueue.add(limit);
    }

    limit.add(ask);
  }

  private void addBid(Order bid) {
    Limit limit = bidLimitMap.get(bid.getPrice());

    if (limit == null) {
      limit = new Limit(bid.getPrice());
      bidLimitMap.put(bid.getPrice(), limit);
      bidLimitQueue.add(limit);
    }

    limit.add(bid);
  }

  private boolean removeAsk(Double price, String orderId) {
    Limit limit = askLimitMap.get(price);
    return limit != null && limit.remove(orderId).isPresent();
  }

  private boolean removeBid(Double price, String orderId) {
    Limit limit = bidLimitMap.get(price);
    return limit != null && limit.remove(orderId).isPresent();
  }

  private List<Order> processAsk(Order ask) {
    Optional<Limit> bestBid = Optional.ofNullable(bidLimitQueue.peek());
    List<Order>     makers  = null;

    if (bestBid.isPresent() && bestBid.get().getPrice() >= ask.getPrice()) {
      makers = bestBid.get().takeLiquidity(ask);
    }

    if (ask.getRemaining() > 0) {
      // todo: keep going till no makers for price
      addAsk(ask);
      return makers;
    } else {
      return new LinkedList<>();
    }
  }

  private List<Order> processBid(Order bid) {
    Optional<Limit> bestAsk = Optional.ofNullable(askLimitQueue.peek());
    List<Order>     makers  = null;

    if (bestAsk.isPresent() && bestAsk.get().getPrice() <= bid.getPrice()) {
      makers = bestAsk.get().takeLiquidity(bid);
    }

    if (bid.getRemaining() > 0) {
      // todo: keep going till no makers for price
      addBid(bid);
      return makers;
    } else {
      return new LinkedList<>();
    }
  }

  public List<Order> add(Order order) {
    if (order.getSide().equals(Order.Side.ASK)) {
      return processAsk(order);
    } else {
      return processBid(order);
    }
  }

  public boolean remove(Order order) {
    if (order.getSide().equals(Order.Side.ASK)) {
      return removeAsk(order.getPrice(), order.getOrderId());
    } else {
      return removeBid(order.getPrice(), order.getOrderId());
    }
  }

  public Optional<Double> getSpread() {
    Optional<Limit> ask = Optional.ofNullable(askLimitQueue.peek());
    Optional<Limit> bid = Optional.ofNullable(bidLimitQueue.peek());

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
