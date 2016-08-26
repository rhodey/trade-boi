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

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import org.anhonesteffort.btc.book.LimitOrderBook;
import org.anhonesteffort.btc.http.request.OrderRequestFactory;
import org.anhonesteffort.btc.http.request.RequestSigner;
import org.anhonesteffort.btc.ws.WsService;
import org.anhonesteffort.btc.state.MatchingStateCurator;
import org.anhonesteffort.btc.state.OrderEvent;
import org.anhonesteffort.btc.strategy.ScamStrategy;
import org.anhonesteffort.btc.strategy.Strategy;
import org.anhonesteffort.btc.util.LongCaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scam {

  private static final Logger log = LoggerFactory.getLogger(Scam.class);

  private static final Integer WS_BUFFER_SIZE  = 512;
  private static final Integer LIMIT_INIT_SIZE =  16;

  private final LongCaster       caster       = new LongCaster(0.000000000001d);
  private final ExecutorService  shutdownPool = Executors.newFixedThreadPool(2);

  private EventHandler<OrderEvent> handlerFor(Strategy ... strategies) {
    return new MatchingStateCurator(
        new LimitOrderBook(LIMIT_INIT_SIZE),
        new HashSet<>(Arrays.asList(strategies))
    );
  }

  @SuppressWarnings("unchecked")
  public void run() throws Exception {
    RequestSigner       signer   = new RequestSigner("9b934b9ea6b87a16613a6b608bddd73e", "nYAbNSqJsIzKe6d27lLJOIbetmYck4PY4OE8noAe1Jap4iLoU6cjGJa6qlatGux6bApw+K8/viE70nwlvwO4lw==", "temp1020");
    OrderRequestFactory requests = new OrderRequestFactory(signer);

    WsService wsService = new WsService(
        new BlockingWaitStrategy(), WS_BUFFER_SIZE,
        new EventHandler[] { handlerFor(new ScamStrategy(requests, caster)) }, caster
    );

    try {

      wsService.start();
      wsService.getShutdownFuture().get();

    } finally {
      log.warn("shutdown procedure initiated");
      shutdownPool.submit(new ShutdownProcedure(shutdownPool, wsService));
    }
  }

  public static void main(String[] args) throws Exception {
    new Scam().run();
  }

}
