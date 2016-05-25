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
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import okhttp3.Request;
import okhttp3.ws.WebSocketCall;
import org.anhonesteffort.btc.state.OrderEvent;
import org.anhonesteffort.btc.http.HttpClient;
import org.anhonesteffort.btc.http.HttpClientWrapper;
import org.anhonesteffort.btc.util.LongCaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class WsService implements ExceptionHandler<OrderEvent>, EventFactory<OrderEvent> {

  private static final Logger log         = LoggerFactory.getLogger(WsService.class);
  private static final String WS_ENDPOINT = "wss://ws-feed.exchange.coinbase.com";

  private final HttpClientWrapper        http           = new HttpClientWrapper();
  private final SettableFuture<Void>     shutdownFuture = SettableFuture.create();
  private final ListeningExecutorService executor       = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

  private final Disruptor<OrderEvent>      wsDisruptor;
  private final EventHandler<OrderEvent>[] handlers;
  private final LongCaster                 caster;

  public WsService(
      WaitStrategy waitStrategy, int bufferSize, EventHandler<OrderEvent>[] handlers, LongCaster caster
  ) {
    this.caster   = caster;
    this.handlers = handlers;
    wsDisruptor   = new Disruptor<>(
        this, bufferSize, new DisruptorThreadFactory(), ProducerType.SINGLE, waitStrategy
    );
  }

  public ListenableFuture<Void> getShutdownFuture() {
    return shutdownFuture;
  }

  @SuppressWarnings("unchecked")
  public void start() {
    WsOrderEventPublisher publisher  = new WsOrderEventPublisher(wsDisruptor.getRingBuffer(), caster);
    WsMessageSorter       sorter     = new WsMessageSorter(publisher, http);
    WsMessageReceiver     wsReceiver = new WsMessageReceiver(new WsSubscribeHelper(executor), sorter);

    wsDisruptor.handleEventsWith(handlers);
    wsDisruptor.setDefaultExceptionHandler(this);

    wsReceiver.getErrorFuture().whenComplete((ok, ex) -> {
      if (ex == null && shutdown()) {
        log.error("websocket error future completed with unknown cause");
      } else if (ex != null && shutdown(ex)) {
        log.error("websocket error", ex);
      }
    });

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
  public void handleOnStartException(Throwable throwable) {
    if (shutdown(throwable)) {
      log.error("error starting disruptor", throwable);
    }
  }

  @Override
  public void handleEventException(Throwable throwable, long sequence, OrderEvent event) {
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

  @Override
  public OrderEvent newInstance() {
    return new OrderEvent();
  }

}
