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

public abstract class Accessor {

  protected double doubleValueOrZero(JsonNode root, String tag) {
    return (root.get(tag) == null) ? 0 : root.get(tag).asDouble(0);
  }

  public String getType(JsonNode root) throws WsException {
    if (root.get("type") != null && root.get("type").isTextual()) {
      return root.get("type").textValue();
    } else {
      throw new WsException("message has invalid type");
    }
  }

}
