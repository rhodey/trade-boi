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

import org.anhonesteffort.btc.compute.ComputeException;
import org.anhonesteffort.btc.http.HttpClientWrapper;
import org.anhonesteffort.btc.http.response.model.GetAccountsResponse;
import org.anhonesteffort.btc.state.State;

import java.io.IOException;
import java.util.Optional;

public class PositionOpenStrategy extends Strategy<Boolean> {

  private Optional<GetAccountsResponse> accounts = Optional.empty();

  public PositionOpenStrategy(HttpClientWrapper http) {
    try {

      http.getAccounts().whenComplete((ok, err) -> {
        if (err == null) {
          accounts = Optional.of(ok);
        } else {
          handleAsyncError(new StrategyException("api request completed with error", err));
        }
      });

    } catch (IOException e) {
      handleAsyncError(new StrategyException("error encoding api request", e));
    }
  }

  @Override
  protected Boolean advanceStrategy(State state, long nanoseconds) {
    return accounts.isPresent();
  }

  @Override
  public void onStateReset() throws ComputeException {
    super.onStateReset();
    throw new StrategyException("unable to handle state reset");
  }

}
