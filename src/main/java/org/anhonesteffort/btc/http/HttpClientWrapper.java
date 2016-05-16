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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.anhonesteffort.btc.http.response.OrderBookResponse;
import org.anhonesteffort.btc.http.response.ResponseParser;

import java.util.concurrent.atomic.AtomicBoolean;

public class HttpClientWrapper {

  private static final String API_BASE = "https://api.exchange.coinbase.com";

  private final OkHttpClient   client   = HttpClient.getInstance();
  private final ResponseParser parser   = new ResponseParser();
  private final AtomicBoolean  shutdown = new AtomicBoolean(false);

  public void shutdown() {
    shutdown.set(true);
  }

  private boolean setFailureIfShutdown(SettableFuture<?> future) {
    if (shutdown.get()) {
      future.setException(new HttpException("this http client wrapper is shutdown"));
      return true;
    } else {
      return false;
    }
  }

  public ListenableFuture<OrderBookResponse> geOrderBook() {
    SettableFuture<OrderBookResponse> future = SettableFuture.create();

    if (!setFailureIfShutdown(future)) {
      client.newCall(new Request.Builder().url(
          API_BASE + "/products/BTC-USD/book?level=3"
      ).build()).enqueue(new OrderBookCallback(parser, future));
    }

    return future;
  }

}
