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

package org.anhonesteffort.btc.event;

import com.lmax.disruptor.EventHandler;
import org.anhonesteffort.btc.book.HeuristicLimitOrderBook;
import org.anhonesteffort.btc.book.MarketOrder;
import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.book.OrderPool;
import org.anhonesteffort.btc.book.TakeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OrderBookBuilder implements EventHandler<OrderEvent> {

  private static final Logger log = LoggerFactory.getLogger(OrderBookBuilder.class);

  protected final HeuristicLimitOrderBook book;
  protected final OrderPool pool;
  private boolean rebuilding = false;

  public OrderBookBuilder(HeuristicLimitOrderBook book, OrderPool pool) {
    this.book = book;
    this.pool = pool;
  }

  protected boolean isRebuilding() {
    return rebuilding;
  }

  protected Order takePooledLimitOrder(OrderEvent event) throws OrderEventException {
    if (event.getPrice() > 0 && event.getSize() > 0) {
      return pool.take(event.getOrderId(), event.getSide(), event.getPrice(), event.getSize());
    } else {
      throw new OrderEventException("open limit order event has invalid price or size");
    }
  }

  protected Order takePooledLimitOrderChange(OrderEvent event) throws OrderEventException {
    if (event.getPrice() > 0 && event.getNewSize() >= 0) {
      return pool.take(event.getOrderId(), event.getSide(), event.getPrice(), event.getNewSize());
    } else {
      throw new OrderEventException("change limit order event has invalid price or new size");
    }
  }

  protected MarketOrder takePooledMarketOrder(OrderEvent event) throws OrderEventException {
    if (event.getSize() > 0 || event.getFunds() > 0) {
      return pool.takeMarket(event.getOrderId(), event.getSide(), event.getSize(), event.getFunds());
    } else {
      throw new OrderEventException("market order event has no size or funds");
    }
  }

  protected MarketOrder takePooledMarketOrderChange(OrderEvent event) throws OrderEventException {
    if (event.getNewSize() >= 0 || event.getNewFunds() >= 0) {
      return pool.takeMarket(event.getOrderId(), event.getSide(), event.getNewSize(), event.getNewFunds());
    } else {
      throw new OrderEventException("change market order event has no new size or new funds");
    }
  }

  protected void returnPooledOrder(Order order) {
    pool.returnOrder(order);
  }

  protected void returnPooledOrders(TakeResult result) {
    result.getMakers().stream()
                      .filter(maker -> maker.getSizeRemaining() <= 0)
                      .forEach(this::returnPooledOrder);
  }

  protected void onRebuildStart() { log.info("rebuilding order book"); }
  protected void onRebuildEnd()   { log.info("order book rebuild complete"); }

  protected abstract void onEvent(OrderEvent event) throws OrderEventException;

  @Override
  public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) throws OrderEventException {
    switch (event.getType()) {
      case REBUILD_START:
        book.clear();
        pool.returnAll();
        rebuilding = true;
        onRebuildStart();
        break;

      case REBUILD_END:
        rebuilding = false;
        onRebuildEnd();
        break;

      default:
        onEvent(event);
    }
  }

}
