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

package org.anhonesteffort.btc.http.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostOrderRequest {

  @JsonProperty private final String product_id;
  @JsonProperty private final String client_oid;
  @JsonProperty private final String type;
  @JsonProperty private final String side;
  @JsonProperty private final String price;
  @JsonProperty private final String size;
  @JsonProperty private final String stp;
  @JsonProperty private final String post_only;

  public PostOrderRequest(String clientOid, String side, String price, String size) {
    product_id = "BTC-USD";
    type       = "limit";
    stp        = "cb";
    post_only  = "true";
    client_oid = clientOid;
    this.side  = side;
    this.price = price;
    this.size  = size;
  }

}
