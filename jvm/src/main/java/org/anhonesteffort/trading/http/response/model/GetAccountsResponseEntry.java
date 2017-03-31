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

package org.anhonesteffort.trading.http.response.model;

import com.fasterxml.jackson.databind.JsonNode;

public class GetAccountsResponseEntry {

  private final String id;
  private final String currency;
  private final String balance;
  private final String available;
  private final String hold;

  public GetAccountsResponseEntry(JsonNode root) {
    id        = root.get("id").textValue();
    currency  = root.get("currency").textValue();
    balance   = root.get("balance").textValue();
    available = root.get("available").textValue();
    hold      = root.get("hold").textValue();
  }

  public String getId() {
    return id;
  }

  public String getCurrency() {
    return currency;
  }

  public String getBalance() {
    return balance;
  }

  public String getAvailable() {
    return available;
  }

  public String getHold() {
    return hold;
  }

}
