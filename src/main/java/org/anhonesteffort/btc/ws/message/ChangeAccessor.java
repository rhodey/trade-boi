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

public class ChangeAccessor extends Accessor {

  public String getOrderId(JsonNode root) throws WsException {
    if (root.get("order_id") != null && root.get("order_id").isTextual()) {
      return root.get("order_id").textValue();
    } else {
      throw new WsException("change message has invalid order_id");
    }
  }

  public float getNewSize(JsonNode root) {
    return floatValueOrZero(root, "new_size");
  }

  public float getOldSize(JsonNode root) {
    return floatValueOrZero(root, "old_size");
  }

  public float getNewFunds(JsonNode root) {
    return floatValueOrZero(root, "new_funds");
  }

  public float getOldFunds(JsonNode root) {
    return floatValueOrZero(root, "old_funds");
  }

  public float getPrice(JsonNode root) {
    return floatValueOrZero(root, "price");
  }

}
