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

import com.lmax.disruptor.EventHandler;
import org.anhonesteffort.btc.book.LimitOrderBook;
import org.anhonesteffort.btc.book.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public abstract class StateCurator implements EventHandler<GdaxEvent> {

  private static final Logger log = LoggerFactory.getLogger(StateCurator.class);

  protected final GdaxState state;
  protected final Set<StateListener> listeners;
  private boolean rebuilding = false;

  public StateCurator(LimitOrderBook book, Set<StateListener> listeners) {
    state          = new GdaxState(book);
    this.listeners = listeners;
  }

  protected boolean isRebuilding() {
    return rebuilding;
  }

  private void cleanupTempState() {
    if (state.getEvent().isPresent()) {
      state.getMakers().stream()
           .filter(make -> make.getSizeRemaining() > 0l)
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
        rebuilding = true;
        log.info("rebuilding order book");
        for (StateListener listener : listeners) { listener.onStateReset(); }
        break;

      case REBUILD_END:
        rebuilding = false;
        log.info("order book rebuild complete");
        break;

      default:
        onEvent(event);
        if (!rebuilding) {
          for (StateListener listener : listeners) { listener.onStateChange(state, event.getNanoseconds()); }
        }
        cleanupTempState();
    }
  }

}
