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

package org.anhonesteffort.btc.ws.message;

import com.fasterxml.jackson.databind.JsonNode;
import org.anhonesteffort.btc.ws.WsException;

public class DoneAccessor extends MarketAccessor {

  public String getOrderType(JsonNode root) throws WsException {
    if (root.get("order_type") != null && root.get("order_type").isTextual()) {
      return root.get("order_type").textValue();
    } else {
      throw new WsException("done message has invalid order_type");
    }
  }

  public String getOrderId(JsonNode root) throws WsException {
    if (root.get("order_id") != null && root.get("order_id").isTextual()) {
      return root.get("order_id").textValue();
    } else {
      throw new WsException("done message has invalid order_id");
    }
  }

  public String getReason(JsonNode root) throws WsException {
    if (root.get("reason") != null && root.get("reason").isTextual()) {
      return root.get("reason").textValue();
    } else {
      throw new WsException("done message has invalid reason");
    }
  }

  public double getPrice(JsonNode root) {
    return doubleValueOrZero(root, "price");
  }

  public double getRemainingSize(JsonNode root) {
    return doubleValueOrZero(root, "remaining_size");
  }

}
