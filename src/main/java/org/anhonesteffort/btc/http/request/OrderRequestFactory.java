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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.anhonesteffort.btc.book.Order;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.Optional;

public class OrderRequestFactory {

  private static final MediaType TYPE_JSON         = MediaType.parse("application/json; charset=utf-8");
  private static final String    API_BASE          = "https://api.exchange.coinbase.com";
  private static final String    API_PATH_ORDERS   = "/orders";
  private static final String    API_PATH_ACCOUNTS = "/accounts";

  private final ObjectWriter  writer = new ObjectMapper().writer();
  private final RequestSigner signer;

  public OrderRequestFactory(RequestSigner signer) {
    this.signer = signer;
  }

  private RequestBody bodyForLimitOrder(String clientOid, Order.Side side, Double price, Double size) throws JsonProcessingException {
    return RequestBody.create(TYPE_JSON, writer.writeValueAsString(
        new OrderRequest(clientOid, (side == Order.Side.ASK) ? "sell" : "buy", price.toString(), size.toString())
    ));
  }

  public Request requestLimitOrder(String clientOid, Order.Side side, Double price, Double size)
      throws IOException, InvalidKeyException, CloneNotSupportedException
  {
    RequestBody     body    = bodyForLimitOrder(clientOid, side, price, size);
    Request.Builder request = new Request.Builder().url(API_BASE + API_PATH_ORDERS).post(body);

    signer.sign(request, "POST", API_PATH_ORDERS, Optional.of(body));

    return request.build();
  }

  public Request requestAccounts()
      throws IOException, InvalidKeyException, CloneNotSupportedException
  {
    Request.Builder request = new Request.Builder().url(API_BASE + API_PATH_ACCOUNTS).get();
    signer.sign(request, "GET", API_PATH_ACCOUNTS, Optional.empty());
    return request.build();
  }

}
