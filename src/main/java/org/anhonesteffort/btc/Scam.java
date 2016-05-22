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

package org.anhonesteffort.btc;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import javafx.application.Application;
import javafx.stage.Stage;
import org.anhonesteffort.btc.book.LimitOrderBook;
import org.anhonesteffort.btc.book.OrderPool;
import org.anhonesteffort.btc.compute.Computation;
import org.anhonesteffort.btc.state.MatchingStateCurator;
import org.anhonesteffort.btc.state.StateCurator;
import org.anhonesteffort.btc.strategy.SpreadStrategy;
import org.anhonesteffort.btc.util.LongCaster;
import org.anhonesteffort.btc.view.OrderBookViewer;
import org.anhonesteffort.btc.ws.WsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class Scam extends Application implements FutureCallback<Void> {

  private static final Logger log = LoggerFactory.getLogger(Scam.class);

  private static final Integer WS_BUFFER_SIZE  = 16384;
  private static final Integer ORDER_POOL_SIZE = 16384;

  private final LongCaster        caster       = new LongCaster(0.000000000001d);
  private final ExecutorService   shutdownPool = Executors.newFixedThreadPool(2);
  private final AtomicBoolean     shuttingDown = new AtomicBoolean(false);
  private       ShutdownProcedure shutdownProcedure;

  @Override
  @SuppressWarnings("unchecked")
  public void start(Stage stage) {
    LimitOrderBook   book     = new LimitOrderBook(16);
    OrderPool        pool     = new OrderPool(ORDER_POOL_SIZE, 64);
    SpreadStrategy   strategy = new SpreadStrategy(caster);
    Set<Computation> compute  = new HashSet<>(Arrays.asList(strategy.getComputations()));
    StateCurator     state    = new MatchingStateCurator(book, pool, compute);

    WsService wsService = new WsService(
        new BlockingWaitStrategy(), WS_BUFFER_SIZE, new EventHandler[] { state }, caster
    );

    shutdownProcedure = new ShutdownProcedure(shutdownPool, wsService);
    Futures.addCallback(wsService.getShutdownFuture(), this);
    wsService.start();

    if (getParameters().getRaw().size() > 0 && getParameters().getRaw().get(0).equals("book")) {
      new OrderBookViewer(book, caster).start(stage);
    }
  }

  @Override
  public void onSuccess(Void aVoid) {
    if (!shuttingDown.getAndSet(true)) {
      log.warn("shutdown procedure initiated");
      shutdownPool.submit(shutdownProcedure);
    }
  }

  @Override
  public void onFailure(Throwable throwable) {
    if (!shuttingDown.getAndSet(true)) {
      log.warn("shutdown procedure initiated");
      shutdownPool.submit(shutdownProcedure);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }

}
