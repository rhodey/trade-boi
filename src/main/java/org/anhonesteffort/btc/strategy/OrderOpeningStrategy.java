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

import org.anhonesteffort.btc.http.HttpClientWrapper;
import org.anhonesteffort.btc.http.request.model.PostOrderRequest;
import org.anhonesteffort.btc.state.CriticalStateProcessingException;
import org.anhonesteffort.btc.state.State;
import org.anhonesteffort.btc.state.StateProcessingException;

import java.io.IOException;
import java.util.Optional;

public class OrderOpeningStrategy extends Strategy<Optional<String>> {

  private final String clientOid;
  private Optional<String> serverOid = Optional.empty();

  public OrderOpeningStrategy(HttpClientWrapper http, PostOrderRequest order) {
    clientOid = order.getClientOid();
    try {

      http.postOrder(order).whenComplete((ok, err) -> {
        if (err != null) {
          handleAsyncError(new CriticalStateProcessingException("api request completed with error", err));
        }
      });

    } catch (IOException e) {
      handleAsyncError(new CriticalStateProcessingException("error encoding api request", e));
    }
  }

  @Override
  protected Optional<String> advanceStrategy(State state, long nanoseconds) {
    if (!serverOid.isPresent()) { serverOid = Optional.ofNullable(state.getOrderIdMap().get(clientOid)); }
    return serverOid;
  }

  @Override
  public void onStateReset() throws StateProcessingException {
    throw new CriticalStateProcessingException("unable to handle state reset");
  }

}
