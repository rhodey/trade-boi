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
import org.anhonesteffort.btc.book.LimitOrderBook;
import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.book.OrderPool;
import org.anhonesteffort.btc.book.TakeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OrderBookProcessor implements EventHandler<OrderEvent> {

  private static final Logger log = LoggerFactory.getLogger(OrderBookProcessor.class);

  protected final CoinbaseState state;
  protected final OrderPool pool;
  private boolean rebuilding = false;
  private long nsTimeSum = 0l;

  public OrderBookProcessor(LimitOrderBook book, OrderPool pool) {
    state     = new CoinbaseState(book);
    this.pool = pool;
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
        state.getOrderBook().clear();
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

    if ((sequence % 50l) == 0l) {
      log.info("avg latency -> " + (nsTimeSum / 50d) + "ns");
      nsTimeSum = System.nanoTime() - event.getNsTime();
    } else {
      nsTimeSum += System.nanoTime() - event.getNsTime();
    }
  }

  protected void onRebuildStart() { log.info("rebuilding order book"); }
  protected void onRebuildEnd()   { log.info("order book rebuild complete"); }

}
