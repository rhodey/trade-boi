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

package org.anhonesteffort.btc.http.response;

import com.fasterxml.jackson.databind.JsonNode;
import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.http.HttpException;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class OrderBookResponse {

  private final Queue<OrderResponse> asks = new PriorityQueue<>(new AskSorter());
  private final Queue<OrderResponse> bids = new PriorityQueue<>(new BidSorter());
  private final long sequence;

  public OrderBookResponse(JsonNode root) throws HttpException {
    if (root.get("sequence") != null && root.get("sequence").isNumber()) {
      this.sequence = root.get("sequence").longValue();
    } else {
      throw new HttpException("json root has invalid sequence tag");
    }

    JsonNode asks = root.path("asks");
    JsonNode bids = root.path("bids");

    if (asks.isArray() && bids.isArray()) {
      while (asks.elements().hasNext()) {
        this.asks.add(new OrderResponse(Order.Side.ASK, asks.elements().next()));
      }
      while (bids.elements().hasNext()) {
        this.bids.add(new OrderResponse(Order.Side.BID, bids.elements().next()));
      }
    } else {
      throw new HttpException("json root has invalid asks and/or bids tag(s)");
    }
  }

  public long getSequence() {
    return sequence;
  }

  public Queue<OrderResponse> getAsks() {
    return asks;
  }

  public Queue<OrderResponse> getBids() {
    return bids;
  }

  private static class AskSorter implements Comparator<OrderResponse> {
    @Override
    public int compare(OrderResponse ask1, OrderResponse ask2) {
      if (ask1.getPrice() < ask2.getPrice()) {
        return -1;
      } else if (ask1.getPrice() == ask2.getPrice()) {
        return 0;
      } else {
        return 1;
      }
    }
  }

  private static class BidSorter implements Comparator<OrderResponse> {
    @Override
    public int compare(OrderResponse bid1, OrderResponse bid2) {
      if (bid1.getPrice() > bid2.getPrice()) {
        return -1;
      } else if (bid1.getPrice() == bid2.getPrice()) {
        return 0;
      } else {
        return 1;
      }
    }
  }

}
