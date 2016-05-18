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
import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.book.OrderPool;
import org.anhonesteffort.btc.book.TakeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PooledOrderBookBuilder implements EventHandler<OrderEvent> {

  private static final Logger log = LoggerFactory.getLogger(PooledOrderBookBuilder.class);

  protected final HeuristicLimitOrderBook book;
  private final OrderPool pool;
  private boolean rebuilding = false;

  public PooledOrderBookBuilder(HeuristicLimitOrderBook book, OrderPool pool) {
    this.book = book;
    this.pool = pool;
  }

  public boolean isRebuilding() {
    return rebuilding;
  }

  protected Order takePooledLimitOrder(OrderEvent event) throws OrderEventException {
    if (event.getPrice() > 0 && event.getSize() > 0) {
      return pool.take(event.getOrderId(), event.getSide(), event.getPrice(), event.getSize());
    } else {
      throw new OrderEventException("open limit order event has invalid price or size");
    }
  }

  protected void returnPooledOrder(Order order) {
    pool.returnOrder(order);
  }

  protected void returnPooledOrders(TakeResult result) {
    result.getMakers().stream().filter(order -> order.getSizeRemaining() <= 0).forEach(pool::returnOrder);
  }

  protected abstract void onEvent(OrderEvent event) throws OrderEventException;

  @Override
  public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) throws OrderEventException {
    switch (event.getType()) {
      case REBUILD_START:
        book.clear(); // todo: init the pool
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

  protected void onRebuildStart() { log.info("rebuilding order book"); }
  protected void onRebuildEnd()   { log.info("order book rebuild complete"); }

}
