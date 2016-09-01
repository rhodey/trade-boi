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
import org.anhonesteffort.btc.state.CriticalStateProcessingException;
import org.anhonesteffort.btc.state.State;
import org.anhonesteffort.btc.state.StateProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class OrderMatchingStrategy extends Strategy<Boolean> {

  private static final Logger log = LoggerFactory.getLogger(OrderMatchingStrategy.class);
  private final String orderId;
  private final long abortNs;
  private long startNs;

  public OrderMatchingStrategy(String orderId, long abortMs) {
    this.orderId = orderId;
    this.abortNs = abortMs * 1_000_000l;
    startNs      = -1l;
  }

  @Override
  protected Boolean advanceStrategy(State state, long nanoseconds) throws StateProcessingException {
    if (state.getCanceled().isPresent() && state.getCanceled().get().equals(orderId)) {
      throw new CriticalStateProcessingException("order canceled unexpectedly");
    } else if (state.getTake().isPresent() && state.getTake().get().getTaker().getOrderId().equals(orderId)) {
      throw new CriticalStateProcessingException("order took from the book");
    } else if (startNs == -1l) {
      startNs = nanoseconds;
    } else if ((nanoseconds - startNs) >= abortNs) {
      abort();
      return false;
    }

    if (!state.getTake().isPresent()) {
      return false;
    }

    Optional<Order> maker = state.getTake().get().getMakers().stream()
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
    throw new CriticalStateProcessingException("unable to handle state reset");
  }

}
