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

package org.anhonesteffort.trading.state;

import com.lmax.disruptor.EventHandler;
import org.anhonesteffort.trading.book.LimitOrderBook;
import org.anhonesteffort.trading.book.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public abstract class StateCurator implements EventHandler<GdaxEvent> {

  private static final Logger log = LoggerFactory.getLogger(StateCurator.class);
  protected static final double FORGIVE_SIZE = 0.000000000001d;

  protected final GdaxState state;
  protected final Set<StateListener> listeners;
  private boolean syncing = false;

  public StateCurator(LimitOrderBook book, Set<StateListener> listeners) {
    state          = new GdaxState(book);
    this.listeners = listeners;
  }

  protected boolean isSyncing() {
    return syncing;
  }

  private void cleanupTempState() {
    if (state.getEvent().isPresent()) {
      state.getMakers().stream()
           .filter(make -> make.getSizeRemaining() > 0d)
           .forEach(Order::clearValueRemoved);
      state.setEvent(null);
      state.getMakers().clear();
    }
  }

  protected abstract void onEvent(GdaxEvent event) throws StateProcessingException;

  @Override
  public void onEvent(GdaxEvent event, long sequence, boolean endOfBatch) throws StateProcessingException {
    switch (event.getType()) {
      case REBUILD_START:
        state.clear();
        syncing = true;
        log.info("syncing order book");
        for (StateListener listener : listeners) { listener.onStateSyncStart(event.getNanoseconds()); }
        break;

      case REBUILD_END:
        syncing = false;
        log.info("order book sync complete");
        for (StateListener listener : listeners) { listener.onStateSyncEnd(event.getNanoseconds()); }
        break;

      default:
        onEvent(event);
        for (StateListener listener : listeners) { listener.onStateChange(state, event.getNanoseconds()); }
        cleanupTempState();
    }
  }

}
