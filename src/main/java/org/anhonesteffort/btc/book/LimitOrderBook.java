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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class LimitOrderBook {

  private final LimitQueue askLimits = new LimitQueue(Order.Side.ASK);
  private final LimitQueue bidLimits = new LimitQueue(Order.Side.BID);

  private List<Order> processAsk(Order ask) {
    List<Order> makers = new LinkedList<>();
    List<Order> next   = bidLimits.takeLiquidityFromBestLimit(ask);

    while (ask.getRemaining() > 0 && !next.isEmpty()) {
      makers.addAll(next);
      next = bidLimits.takeLiquidityFromBestLimit(ask);
    }

    if (ask.getRemaining() > 0) {
      askLimits.addOrder(ask);
    }

    return makers;
  }

  private List<Order> processBid(Order bid) {
    List<Order> makers = new LinkedList<>();
    List<Order> next   = askLimits.takeLiquidityFromBestLimit(bid);

    while (bid.getRemaining() > 0 && !next.isEmpty()) {
      makers.addAll(next);
      next = askLimits.takeLiquidityFromBestLimit(bid);
    }

    if (bid.getRemaining() > 0) {
      bidLimits.addOrder(bid);
    }

    return makers;
  }

  public List<Order> add(Order order) {
    if (order.getSide().equals(Order.Side.ASK)) {
      return processAsk(order);
    } else {
      return processBid(order);
    }
  }

  public Optional<Order> remove(Order.Side side, Double price, String orderId) {
    if (side.equals(Order.Side.ASK)) {
      return askLimits.removeOrder(price, orderId);
    } else {
      return bidLimits.removeOrder(price, orderId);
    }
  }

  public Optional<Double> getSpread() {
    Optional<Limit> bestAsk = askLimits.peek();
    Optional<Limit> bestBid = bidLimits.peek();

    if (bestAsk.isPresent() && bestBid.isPresent()) {
      return Optional.of(bestAsk.get().getPrice() - bestBid.get().getPrice());
    } else {
      return Optional.empty();
    }
  }

}
