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
import org.anhonesteffort.btc.util.LongCaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OrderBookBuilder implements EventHandler<OrderEvent> {

  private static final Logger log = LoggerFactory.getLogger(OrderBookBuilder.class);

  protected final HeuristicLimitOrderBook book;
  protected final OrderPool               pool;
  protected final LongCaster              caster;

  private boolean rebuilding = false;

  public OrderBookBuilder(HeuristicLimitOrderBook book, OrderPool pool, LongCaster caster) {
    this.book   = book;
    this.pool   = pool;
    this.caster = caster;
  }

  protected long toLong(float value) {
    return caster.fromDouble(value);
  }

  protected boolean isRebuilding() {
    return rebuilding;
  }

  protected void returnPooledOrder(Order order) {
    pool.returnOrder(order);
  }

  protected void returnPooledOrders(TakeResult result) {
    result.getMakers().stream()
                      .filter(maker -> maker.getSizeRemaining() <= 0l)
                      .forEach(this::returnPooledOrder);
  }

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

  protected void onRebuildStart() { log.info("rebuilding order book"); }
  protected void onRebuildEnd()   { log.info("order book rebuild complete"); }

}
