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

package org.anhonesteffort.btc.strategy;

import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.state.OrderEvent;
import org.anhonesteffort.btc.state.StateProcessingException;
import org.anhonesteffort.btc.state.GdaxState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class OrderMatchingStrategy extends AbortableStrategy<Boolean> {

  private static final Logger log = LoggerFactory.getLogger(OrderMatchingStrategy.class);
  private final String orderId;

  public OrderMatchingStrategy(String orderId) {
    this.orderId = orderId;
  }

  protected abstract boolean shouldAbort(GdaxState state, long nanoseconds);

  @Override
  protected Boolean advanceStrategy(GdaxState state, long nanoseconds) throws StateProcessingException {
    if (state.getEvent().isPresent() && state.getEvent().get().getType() != OrderEvent.Type.OPEN &&
        state.getEvent().get().getOrderId().equals(orderId))
    {
      throw new StateProcessingException("order took, reduced, or canceled unexpectedly");
    } else if (shouldAbort(state, nanoseconds)) {
      abort();
      return false;
    }

    Optional<Order> maker = state.getMakers().stream()
                                 .filter(m -> m.getOrderId().equals(orderId))
                                 .findAny();

    if (!maker.isPresent()) {
      return false;
    } else if (maker.get().getSizeRemaining() > 0l) {
      log.info("order partially matched, " + maker.get().getSizeRemaining() + " remaining");
      return false;
    } else {
      return true;
    }
  }

  @Override
  public void onStateReset() throws StateProcessingException {
    throw new StateProcessingException("unable to handle state reset");
  }

}
