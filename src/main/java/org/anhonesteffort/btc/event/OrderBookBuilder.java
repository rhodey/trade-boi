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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderBookBuilder implements EventHandler<OrderEvent> {

  private static final Logger log = LoggerFactory.getLogger(OrderBookBuilder.class);

  private final HeuristicLimitOrderBook book;

  public OrderBookBuilder(HeuristicLimitOrderBook book) {
    this.book = book;
  }

  @Override
  public void onEvent(OrderEvent event, long sequence, boolean endOfBatch) throws OrderEventException {
    log.info("received -> " + event.getType());

    switch (event.getType()) {
      case REBUILD_START:
        log.info("rebuilding order book");
        book.clear();
        break;

      case REBUILD_END:
        log.info("order book rebuild complete");
        break;

      case LIMIT_OPEN:
        break;

      case LIMIT_DONE:
        break;

      case LIMIT_CHANGE:
        break;
    }

  }

}
