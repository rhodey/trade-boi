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

package org.anhonesteffort.btc.http.response.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.anhonesteffort.trading.book.Order;
import org.anhonesteffort.trading.http.HttpException;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

public class GetOrderBookResponse {

  private final Queue<GetOrderBookResponseEntry> asks = new ArrayDeque<>(500);
  private final Queue<GetOrderBookResponseEntry> bids = new ArrayDeque<>(500);
  private final long sequence;

  public GetOrderBookResponse(JsonNode root) throws HttpException {
    if (root.get("sequence") != null && root.get("sequence").isNumber()) {
      this.sequence = root.get("sequence").longValue();
    } else {
      throw new HttpException("json root has invalid sequence tag");
    }

    Iterator<JsonNode> asks = root.path("asks").elements();
    Iterator<JsonNode> bids = root.path("bids").elements();

    if (root.path("asks").isArray() && root.path("bids").isArray()) {
      while (asks.hasNext()) {
        this.asks.add(new GetOrderBookResponseEntry(Order.Side.ASK, asks.next()));
      }
      while (bids.hasNext()) {
        this.bids.add(new GetOrderBookResponseEntry(Order.Side.BID, bids.next()));
      }
    } else {
      throw new HttpException("json root has invalid asks and/or bids tag(s)");
    }
  }

  public long getSequence() {
    return sequence;
  }

  public Queue<GetOrderBookResponseEntry> getAsks() {
    return asks;
  }

  public Queue<GetOrderBookResponseEntry> getBids() {
    return bids;
  }

}
