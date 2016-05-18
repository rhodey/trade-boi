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

public class MarketAccessor extends Accessor {

  public long getSequence(JsonNode root) throws WsException {
    if (root.get("sequence") != null && root.get("sequence").isNumber()) {
      return root.get("sequence").longValue();
    } else {
      throw new WsException("message has invalid sequence");
    }
  }

  public String getTime(JsonNode root) throws WsException {
    if (root.get("time") != null && root.get("time").isTextual()) {
      return root.get("time").textValue();
    } else {
      throw new WsException("message has invalid time");
    }
  }

  public String getProductId(JsonNode root) throws WsException {
    if (root.get("product_id") != null && root.get("product_id").isTextual()) {
      return root.get("product_id").textValue();
    } else {
      throw new WsException("message has invalid product_id");
    }
  }

  public String getSide(JsonNode root) throws WsException {
    if (root.get("side") != null && root.get("side").isTextual()) {
      return root.get("side").textValue();
    } else {
      throw new WsException("message has invalid side");
    }
  }

}
