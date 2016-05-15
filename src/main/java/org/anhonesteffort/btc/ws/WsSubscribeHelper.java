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

package org.anhonesteffort.btc.ws;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import okhttp3.RequestBody;
import okhttp3.ws.WebSocket;

import java.io.IOException;
import java.util.concurrent.Callable;

public class WsSubscribeHelper {

  private static final String SUBSCRIBE = "{ \"type\": \"subscribe\", \"product_id\": \"BTC-USD\" }";
  private final ListeningExecutorService executor;

  public WsSubscribeHelper(ListeningExecutorService executor) {
    this.executor = executor;
  }

  public ListenableFuture<Void> subscribe(WebSocket socket) {
    return executor.submit(new Sender(socket, SUBSCRIBE));
  }

  private static class Sender implements Callable<Void> {

    private final WebSocket socket;
    private final String    message;

    public Sender(WebSocket socket, String message) {
      this.socket  = socket;
      this.message = message;
    }

    @Override
    public Void call() throws IOException {
      socket.sendMessage(RequestBody.create(WebSocket.TEXT, message));
      return null;
    }

  }

}
