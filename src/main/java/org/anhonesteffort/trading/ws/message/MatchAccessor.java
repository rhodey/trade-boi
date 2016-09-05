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

package org.anhonesteffort.trading.ws.message;

import com.fasterxml.jackson.databind.JsonNode;
import org.anhonesteffort.trading.ws.WsException;

public class MatchAccessor extends Accessor {

  public long getTradeId(JsonNode root) throws WsException {
    if (root.get("trade_id") != null && root.get("trade_id").isNumber()) {
      return root.get("trade_id").longValue();
    } else {
      throw new WsException("match message has invalid trade_id");
    }
  }

  public String getMakerOrderId(JsonNode root) throws WsException {
    if (root.get("maker_order_id") != null && root.get("maker_order_id").isTextual()) {
      return root.get("maker_order_id").textValue();
    } else {
      throw new WsException("match message has invalid maker_order_id");
    }
  }

  public String getTakerOrderId(JsonNode root) throws WsException {
    if (root.get("taker_order_id") != null && root.get("taker_order_id").isTextual()) {
      return root.get("taker_order_id").textValue();
    } else {
      throw new WsException("match message has invalid taker_order_id");
    }
  }

  public double getSize(JsonNode root) throws WsException {
    if (root.get("size") != null && root.get("size").isTextual()) {
      try {

        return Double.parseDouble(root.get("size").textValue());

      } catch (NumberFormatException e) {
        throw new WsException("math message has invalid size", e);
      }
    } else {
      throw new WsException("match message has invalid size");
    }
  }

  public double getPrice(JsonNode root) throws WsException {
    if (root.get("price") != null && root.get("price").isTextual()) {
      try {

        return Double.parseDouble(root.get("price").textValue());

      } catch (NumberFormatException e) {
        throw new WsException("math message has invalid price", e);
      }
    } else {
      throw new WsException("match message has invalid price");
    }
  }

}
