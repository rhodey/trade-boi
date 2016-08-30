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

package org.anhonesteffort.btc.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.anhonesteffort.btc.http.response.GetAccountsCallback;
import org.anhonesteffort.btc.http.response.PostOrderCallback;
import org.anhonesteffort.btc.http.request.model.PostOrderRequest;
import org.anhonesteffort.btc.http.request.RequestSigner;
import org.anhonesteffort.btc.http.response.GetOrderBookCallback;
import org.anhonesteffort.btc.http.response.model.GetAccountsResponse;
import org.anhonesteffort.btc.http.response.model.GetOrderBookResponse;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpClientWrapper {

  private static final MediaType TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

  private static final String API_BASE          = "https://api.exchange.coinbase.com";
  private static final String API_PATH_BOOK     = "/products/BTC-USD/book?level=3";
  private static final String API_PATH_ORDERS   = "/orders";
  private static final String API_PATH_ACCOUNTS = "/accounts";

  private final OkHttpClient  client   = HttpClient.getInstance();
  private final ObjectReader  reader   = new ObjectMapper().reader();
  private final ObjectWriter  writer   = new ObjectMapper().writer();
  private final AtomicBoolean shutdown = new AtomicBoolean(false);
  private final RequestSigner signer;

  public HttpClientWrapper(RequestSigner signer) {
    this.signer = signer;
  }

  public void shutdown() {
    shutdown.set(true);
  }

  private boolean setExceptionIfShutdown(CompletableFuture<?> future) {
    if (shutdown.get()) {
      future.completeExceptionally(new HttpException("this http client wrapper is shutdown"));
      return true;
    } else {
      return false;
    }
  }

  public CompletableFuture<GetOrderBookResponse> geOrderBook() {
    CompletableFuture<GetOrderBookResponse> future = new CompletableFuture<>();

    if (!setExceptionIfShutdown(future)) {
      client.newCall(new Request.Builder().url(
          API_BASE + API_PATH_BOOK
      ).build()).enqueue(new GetOrderBookCallback(reader, future));
    }

    return future;
  }

  public CompletableFuture<GetAccountsResponse> getAccounts() throws IOException {
    CompletableFuture<GetAccountsResponse> future = new CompletableFuture<>();

    if (!setExceptionIfShutdown(future)) {
      Request.Builder request = new Request.Builder().url(API_BASE + API_PATH_ACCOUNTS).get();
      signer.sign(request, "GET", API_PATH_ACCOUNTS, Optional.empty());
      client.newCall(request.build()).enqueue(new GetAccountsCallback(reader, future));
    }

    return future;
  }

  public CompletableFuture<Response> postOrder(PostOrderRequest order) throws IOException {
    CompletableFuture<Response> future = new CompletableFuture<>();

    if (!setExceptionIfShutdown(future)) {
      RequestBody     body    = RequestBody.create(TYPE_JSON, writer.writeValueAsString(order));
      Request.Builder request = new Request.Builder().url(API_BASE + API_PATH_ORDERS).post(body);
      signer.sign(request, "POST", API_PATH_ORDERS, Optional.of(body));
      client.newCall(request.build()).enqueue(new PostOrderCallback(future));
    }

    return future;
  }

}
