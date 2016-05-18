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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import okhttp3.Request;
import okhttp3.ws.WebSocketCall;
import org.anhonesteffort.btc.http.HttpClient;
import org.anhonesteffort.btc.http.HttpClientWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class WsService implements FutureCallback<Void>, ExceptionHandler<Message> {

  private static final Logger log         = LoggerFactory.getLogger(WsService.class);
  private static final String WS_ENDPOINT = "wss://ws-feed.exchange.coinbase.com";

  private final MessageDecoder           decoder        = new MessageDecoder();
  private final HttpClientWrapper        http           = new HttpClientWrapper();
  private final SettableFuture<Void>     shutdownFuture = SettableFuture.create();
  private final ListeningExecutorService executor       = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

  private final Disruptor<Message> wsDisruptor;

  public WsService(WaitStrategy waitStrategy, int bufferSize) {
    wsDisruptor = new Disruptor<>(
        decoder, bufferSize, new DisruptorThreadFactory(), ProducerType.SINGLE, waitStrategy
    );
  }

  public ListenableFuture<Void> getShutdownFuture() {
    return shutdownFuture;
  }

  public void start() {
    WsMessageProcessor wsProcessor = new WsMessageProcessor(http);
    WsMessageReceiver  wsReceiver  = new WsMessageReceiver(
        wsDisruptor.getRingBuffer(), decoder, new WsSubscribeHelper(executor)
    );

    wsDisruptor.handleEventsWith(wsProcessor);
    wsDisruptor.setDefaultExceptionHandler(this);
    Futures.addCallback(wsReceiver.getErrorFuture(), this);

    wsDisruptor.start();
    WebSocketCall.create(
        HttpClient.getInstance(), new Request.Builder().url(WS_ENDPOINT).build()
    ).enqueue(wsReceiver);
  }

  public boolean shutdown() {
    if (shutdownFuture.set(null)) {
      http.shutdown();
      return true;
    } else {
      return false;
    }
  }

  private boolean shutdown(Throwable throwable) {
    if (shutdownFuture.setException(throwable)) {
      http.shutdown();
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void onSuccess(Void aVoid) {
    if (shutdown()) {
      log.error("websocket error future completed with unknown cause");
    }
  }

  @Override
  public void onFailure(Throwable throwable) {
    if (shutdown(throwable)) {
      log.error("websocket error", throwable);
    }
  }

  @Override
  public void handleOnStartException(Throwable throwable) {
    if (shutdown(throwable)) {
      log.error("error starting disruptor", throwable);
    }
  }

  @Override
  public void handleEventException(Throwable throwable, long sequence, Message message) {
    if (shutdown(throwable)) {
      log.error("error processing disruptor event", throwable);
    }
  }

  @Override
  public void handleOnShutdownException(Throwable throwable) {
    if (shutdown(throwable)) {
      log.error("error shutting down disruptor", throwable);
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
