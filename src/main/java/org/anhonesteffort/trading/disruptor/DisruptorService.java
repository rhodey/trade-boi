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
package org.anhonesteffort.trading.disruptor;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.anhonesteffort.trading.ScamConfig;
import org.anhonesteffort.trading.Service;
import org.anhonesteffort.trading.state.GdaxEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;

public class DisruptorService implements Service, ExceptionHandler<GdaxEvent>, EventFactory<GdaxEvent> {

  private final CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();
  private final Disruptor<GdaxEvent> wsDisruptor;
  private final EventHandler[] handlers;

  public DisruptorService(ScamConfig config, WaitStrategy waitStrategy, EventHandler[] handlers) {
    this.handlers = handlers;
    wsDisruptor   = new Disruptor<>(
        this, config.getWsBufferSize(), new DisruptorThreadFactory(),
        ProducerType.SINGLE, waitStrategy
    );
  }

  public RingBuffer<GdaxEvent> ringBuffer() {
    return wsDisruptor.getRingBuffer();
  }

  @Override
  public CompletableFuture<Void> shutdownFuture() {
    return shutdownFuture;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void start() {
    wsDisruptor.setDefaultExceptionHandler(this);
    wsDisruptor.handleEventsWith(handlers);
    wsDisruptor.start();
  }

  @Override
  public boolean shutdown() {
    if (shutdownFuture.complete(null)) {
      wsDisruptor.shutdown();
      return true;
    } else {
      return false;
    }
  }

  private boolean shutdown(Throwable throwable) {
    if (shutdownFuture.completeExceptionally(throwable)) {
      wsDisruptor.shutdown();
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void handleOnStartException(Throwable throwable) {
    shutdown(throwable);
  }

  @Override
  public void handleEventException(Throwable throwable, long sequence, GdaxEvent event) {
    shutdown(throwable);
  }

  @Override
  public void handleOnShutdownException(Throwable throwable) {
    shutdown(throwable);
  }

  private static class DisruptorThreadFactory implements ThreadFactory {
    private int count = 0;

    @Override
    public Thread newThread(Runnable runnable) {
      Thread thread = new Thread(runnable, "disruptor-" + (count++));
      thread.setDaemon(true);
      return thread;
    }
  }

  @Override
  public GdaxEvent newInstance() {
    return new GdaxEvent();
  }

}
