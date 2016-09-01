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
import org.anhonesteffort.btc.http.request.RequestFactory;
import org.anhonesteffort.btc.http.request.model.PostOrderRequest;
import org.anhonesteffort.btc.state.State;
import org.anhonesteffort.btc.util.LongCaster;

import java.util.Optional;

public class AskIdentifyingStrategy extends Strategy<Optional<PostOrderRequest>> {

  private final RequestFactory requests = new RequestFactory();
  private final LongCaster caster;
  private Optional<Order> lastAsk = Optional.empty();
  private Optional<Order> bidPosition = Optional.empty();

  public AskIdentifyingStrategy(LongCaster caster) {
    this.caster = caster;
  }

  public void setContext(Optional<Order> bidPosition, Optional<Order> lastAsk) {
    this.bidPosition = bidPosition;
    this.lastAsk     = lastAsk;
  }

  private Optional<PostOrderRequest> askOrder(double price) {
    return Optional.of(requests.newOrder(
        Order.Side.ASK, price, caster.toDouble(bidPosition.get().getSize()))
    );
  }

  @Override
  protected Optional<PostOrderRequest> advanceStrategy(State state, long nanoseconds) {
    if (!bidPosition.isPresent()) { return Optional.empty(); }

    double askCeiling = caster.toDouble(state.getOrderBook().getAskLimits().peek().get().getPrice());
    double lastPrice  = lastAsk.isPresent() ? caster.toDouble(lastAsk.get().getPrice()) : -1l;
    double bidPrice   = caster.toDouble(bidPosition.get().getPrice());

    if (!lastAsk.isPresent()) {
      return askOrder(askCeiling);
    } else if (lastPrice > bidPrice) {
      return askOrder(lastPrice - 0.01d);
    } else {
      return askOrder(bidPrice);
    }
  }

}
