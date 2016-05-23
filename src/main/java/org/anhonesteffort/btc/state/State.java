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

package org.anhonesteffort.btc.state;

import org.anhonesteffort.btc.book.LimitOrderBook;
import org.anhonesteffort.btc.book.MarketOrder;
import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.book.TakeResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class State {

  private final Map<String, MarketOrder> marketOrders  = new HashMap<>();
  private final Map<String, Order>       rxLimitOrders = new HashMap<>();
  private final Set<TakeResult>          takes         = new HashSet<>();

  private final LimitOrderBook orderBook;

  public State(LimitOrderBook orderBook) {
    this.orderBook = orderBook;
  }

  public LimitOrderBook getOrderBook() {
    return orderBook;
  }

  public Set<TakeResult> getTakes() {
    return takes;
  }

  public Map<String, Order> getRxLimitOrders() {
    return rxLimitOrders;
  }

  public Map<String, MarketOrder> getMarketOrders() {
    return marketOrders;
  }

  public void clear() {
    orderBook.clear();
    takes.clear();
    rxLimitOrders.clear();
    marketOrders.clear();
  }

}
