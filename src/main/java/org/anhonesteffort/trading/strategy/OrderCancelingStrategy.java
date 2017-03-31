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

package org.anhonesteffort.trading.strategy;

import org.anhonesteffort.trading.http.HttpClientWrapper;
import org.anhonesteffort.trading.state.StateProcessingException;
import org.anhonesteffort.trading.state.GdaxState;

import java.io.IOException;

public class OrderCancelingStrategy extends Strategy<Boolean> {

  private volatile boolean canceled = false;

  public OrderCancelingStrategy(HttpClientWrapper http, String orderId) {
    try {

      http.cancelOrder(orderId).whenComplete((ok, err) -> {
        if (err != null) {
          handleAsyncError(new StateProcessingException("cancel order request completed with error", err));
        } else {
          canceled = true;
        }
      });

    } catch (IOException e) {
      handleAsyncError(new StateProcessingException("error encoding api request", e));
    }
  }

  @Override
  protected Boolean advanceStrategy(GdaxState state, long nanoseconds) {
    return canceled;
  }

}
