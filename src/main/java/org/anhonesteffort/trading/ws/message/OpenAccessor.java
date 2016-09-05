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

public class OpenAccessor extends Accessor {

  public String getOrderId(JsonNode root) throws WsException {
    if (root.get("order_id") != null && root.get("order_id").isTextual()) {
      return root.get("order_id").textValue();
    } else {
      throw new WsException("open message has invalid order_id");
    }
  }

  public double getPrice(JsonNode root) throws WsException {
    if (root.get("price") != null && root.get("price").isTextual()) {
      try {

        return Double.parseDouble(root.get("price").textValue());

      } catch (NumberFormatException e) {
        throw new WsException("open message has invalid price", e);
      }
    } else {
      throw new WsException("open message has invalid price");
    }
  }

  public double getRemainingSize(JsonNode root) throws WsException {
    if (root.get("remaining_size") != null && root.get("remaining_size").isTextual()) {
      try {

        return Double.parseDouble(root.get("remaining_size").textValue());

      } catch (NumberFormatException e) {
        throw new WsException("open message has invalid remaining_size", e);
      }
    } else {
      throw new WsException("open message has invalid remaining_size");
    }
  }

}
