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

import java.io.IOException;

public class Message {

  public static final String TYPE_RECEIVED = "received";
  public static final String TYPE_OPEN     = "open";
  public static final String TYPE_DONE     = "done";
  public static final String TYPE_MATCH    = "match";
  public static final String TYPE_CHANGE   = "change";
  public static final String TYPE_ERROR    = "error";

  protected JsonNode root;
  protected String   type;

  protected Message() { }

  protected void init(JsonNode root) throws IOException {
    this.root = root;

    if (root.get("type") != null && !root.get("type").isNull()) {
      this.type = root.get("type").textValue();
    } else {
      throw new IOException("json root has invalid type tag");
    }
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return "type: " + type;
  }

}
