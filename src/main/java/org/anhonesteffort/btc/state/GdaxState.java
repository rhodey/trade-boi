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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GdaxState {

  private final Map<String, String>      orderIdMap    = new HashMap<>();
  private final Map<String, Order>       rxLimitOrders = new HashMap<>();
  private final Map<String, MarketOrder> marketOrders  = new HashMap<>();
  private final List<Order>              makers        = new ArrayList<>();
  private       Optional<OrderEvent>     event         = Optional.empty();

  private final LimitOrderBook orderBook;

  public GdaxState(LimitOrderBook orderBook) {
    this.orderBook = orderBook;
  }

  public LimitOrderBook getOrderBook() {
    return orderBook;
  }

  public Map<String, String> getOrderIdMap() {
    return orderIdMap;
  }

  public Map<String, Order> getRxLimitOrders() {
    return rxLimitOrders;
  }

  public Map<String, MarketOrder> getMarketOrders() {
    return marketOrders;
  }

  public List<Order> getMakers() {
    return makers;
  }

  public void setEvent(OrderEvent event) {
    this.event = Optional.ofNullable(event);
  }

  public Optional<OrderEvent> getEvent() {
    return event;
  }

  public void clear() {
    orderBook.clear();
    orderIdMap.clear();
    rxLimitOrders.clear();
    marketOrders.clear();
    makers.clear();
    event    = Optional.empty();
  }

}
