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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.anhonesteffort.btc.http.response.OrderBookCallback;
import org.anhonesteffort.btc.http.response.OrderBookResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpClientWrapper {

  private static final String API_BASE = "https://api.exchange.coinbase.com";

  private final OkHttpClient  client   = HttpClient.getInstance();
  private final ObjectReader  reader   = new ObjectMapper().reader();
  private final AtomicBoolean shutdown = new AtomicBoolean(false);

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

  public CompletableFuture<OrderBookResponse> geOrderBook() {
    CompletableFuture<OrderBookResponse> future = new CompletableFuture<>();

    if (!setExceptionIfShutdown(future)) {
      client.newCall(new Request.Builder().url(
          API_BASE + "/products/BTC-USD/book?level=3"
      ).build()).enqueue(new OrderBookCallback(reader, future));
    }

    return future;
  }

}
