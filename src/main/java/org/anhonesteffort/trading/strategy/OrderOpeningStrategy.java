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
import org.anhonesteffort.trading.http.request.model.PostOrderRequest;
import org.anhonesteffort.trading.state.StateProcessingException;
import org.anhonesteffort.trading.state.GdaxState;
import org.anhonesteffort.trading.book.Order;

import java.io.IOException;
import java.util.Optional;

public class OrderOpeningStrategy extends AbortableStrategy<Optional<Order>> {

  private final PostOrderRequest postOrder;

  public OrderOpeningStrategy(HttpClientWrapper http, PostOrderRequest postOrder) {
    this.postOrder = postOrder;
    try {

      http.postOrder(postOrder).whenComplete((ok, err) -> {
        if (err != null) {
          handleAsyncError(new StateProcessingException("post order request completed with error", err));
        } else if (!ok) {
          abort(); // todo: not thread safe
        }
      });

    } catch (IOException e) {
      handleAsyncError(new StateProcessingException("error encoding api request", e));
    }
  }

  private boolean sideMatches(PostOrderRequest postOrder, Order bookOrder) {
    return (postOrder.getSide().equals("sell") && bookOrder.getSide() == Order.Side.ASK) ||
           (postOrder.getSide().equals("buy")  && bookOrder.getSide() == Order.Side.BID);
  }

  @Override
  protected Optional<Order> advanceStrategy(GdaxState state, long nanoseconds) throws StateProcessingException {
    Optional<String> bookOid   = Optional.ofNullable(state.getClientOIdMap().get(postOrder.getClientOid()));
    Optional<Order>  bookOrder = bookOid.isPresent() ?
        Optional.ofNullable(state.getRxLimitOrders().get(bookOid.get())) : Optional.empty();

    if (isSyncing()) {
      throw new StateProcessingException("unable to handle state synchronization");
    } else if (!bookOid.isPresent()) {
      return Optional.empty();
    } else if (!bookOrder.isPresent()) {
      throw new StateProcessingException("order id map entry not found in rx limit order map");
    } else if (!sideMatches(postOrder, bookOrder.get())) {
      throw new StateProcessingException("posted order ended up on wrong side of the book");
    } else {
      return bookOrder;
    }
  }

}