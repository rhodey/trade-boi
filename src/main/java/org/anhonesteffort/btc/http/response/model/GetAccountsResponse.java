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
import org.anhonesteffort.trading.http.HttpException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GetAccountsResponse {

  private final List<GetAccountsResponseEntry> accounts = new LinkedList<>();

  public GetAccountsResponse(JsonNode root) throws HttpException {
    if (!root.isArray()) {
      throw new HttpException("json root is not array");
    } else {
      Iterator<JsonNode> elements = root.elements();
      while (elements.hasNext()) { accounts.add(new GetAccountsResponseEntry(elements.next())); }
    }
  }

  public List<GetAccountsResponseEntry> getAccounts() {
    return accounts;
  }

}
