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
import com.lmax.disruptor.YieldingWaitStrategy;
import org.anhonesteffort.btc.book.LimitOrderBook;
import org.anhonesteffort.btc.http.HttpClientWrapper;
import org.anhonesteffort.btc.state.StateListener;
import org.anhonesteffort.btc.stats.StatsHandlerFactory;
import org.anhonesteffort.btc.stats.StatsService;
import org.anhonesteffort.btc.ws.WsService;
import org.anhonesteffort.btc.state.MatchingStateCurator;
import org.anhonesteffort.btc.state.OrderEvent;
import org.anhonesteffort.btc.strategy.ScamStrategy;
import org.anhonesteffort.btc.util.LongCaster;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scam {

  private final LongCaster        caster = new LongCaster(0.000000000001d);
  private final ExecutorService   pool   = Executors.newFixedThreadPool(2);
  private final ScamConfig        config;
  private final HttpClientWrapper http;

  public Scam() throws IOException, NoSuchAlgorithmException {
    config = new ScamConfig();
    http   = new HttpClientWrapper(config);
  }

  private EventHandler<OrderEvent> handlerFor(StateListener... listeners) {
    return new MatchingStateCurator(
        new LimitOrderBook(config.getLimitInitSize()),
        new HashSet<>(Arrays.asList(listeners))
    );
  }

  public void run() throws Exception {
    ScamStrategy        scamStrategy = new ScamStrategy(http, caster);
    StatsHandlerFactory statsHandler = new StatsHandlerFactory();

    if (config.getStatsEnabled()) {
      config.setWaitStrategy(new BlockingWaitStrategy());
      config.setEventHandlers(new EventHandler[]{handlerFor(scamStrategy, statsHandler)});
    } else {
      config.setWaitStrategy(new YieldingWaitStrategy());
      config.setEventHandlers(new EventHandler[]{handlerFor(scamStrategy)});
    }

    WsService    wsService    = new WsService(config, http, caster);
    StatsService statsService = new StatsService(config, statsHandler);

    wsService.start();
    if (config.getStatsEnabled()) { statsService.start(); }

    pool.submit(new ShutdownProcedure(pool, wsService, statsService, http)).get();
  }

  public static void main(String[] args) throws Exception {
    new Scam().run();
  }

}
