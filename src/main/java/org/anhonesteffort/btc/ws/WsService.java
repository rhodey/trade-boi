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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import okhttp3.Request;
import okhttp3.ws.WebSocketCall;
import org.anhonesteffort.btc.http.HttpClient;
import org.anhonesteffort.btc.message.Message;
import org.anhonesteffort.btc.message.MessageDecoder;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class WsService {

  private static final String WS_ENDPOINT = "wss://ws-feed.exchange.coinbase.com";

  private final MessageDecoder           decoder  = new MessageDecoder();
  private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

  private final Disruptor<Message> wsDisruptor;
  private       WsMessageReceiver  wsReceiver;

  public WsService(WaitStrategy waitStrategy, int bufferSize) {
    wsDisruptor = new Disruptor<>(
        decoder, bufferSize, new DisruptorThreadFactory(), ProducerType.SINGLE, waitStrategy
    );
  }

  @SuppressWarnings("unchecked")
  public void start(WsErrorCallback errorCb) {
    WsSubscribeHelper    wsHelper = new WsSubscribeHelper(executor);
    SettableFuture<Void> wsError  = SettableFuture.create();

    wsReceiver = new WsMessageReceiver(
        wsDisruptor.getRingBuffer(), decoder, wsHelper, wsError
    );

    wsDisruptor.handleEventsWith(new WsMessageProcessor());
    wsDisruptor.setDefaultExceptionHandler(errorCb);
    Futures.addCallback(wsError, errorCb);

    wsDisruptor.start();
    WebSocketCall.create(
        HttpClient.getInstance(), new Request.Builder().url(WS_ENDPOINT).build()
    ).enqueue(wsReceiver);
  }

  public void stop() throws IOException {
    try {

      wsReceiver.closeSocket();

    } finally {
      executor.shutdownNow();
      wsDisruptor.shutdown();
    }
  }

  private static class DisruptorThreadFactory implements ThreadFactory {
    private int count = 0;

    @Override
    public Thread newThread(Runnable runnable) {
      Thread thread = new Thread(runnable, "ws-disrupt-" + (count++));
      thread.setDaemon(true);
      return thread;
    }
  }

}
