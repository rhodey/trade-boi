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

public class ChangeAccessor extends Accessor {

  public String getOrderId(JsonNode root) {
    return root.get("order_id").textValue();
  }

  public double getNewSize(JsonNode root) {
    return doubleValueOrZero(root, "new_size");
  }

  public double getOldSize(JsonNode root) {
    return doubleValueOrZero(root, "old_size");
  }

  public double getNewFunds(JsonNode root) {
    return doubleValueOrZero(root, "new_funds");
  }

  public double getOldFunds(JsonNode root) {
    return doubleValueOrZero(root, "old_funds");
  }

  public double getPrice(JsonNode root) {
    return doubleValueOrZero(root, "price");
  }

}
