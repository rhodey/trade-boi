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

package org.anhonesteffort.trading;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import org.anhonesteffort.trading.book.CompatLimitOrderBook;
import org.anhonesteffort.trading.disruptor.DisruptorService;
import org.anhonesteffort.trading.dsl.Runtime.DslContext;
import org.anhonesteffort.trading.http.HttpClientWrapper;
import org.anhonesteffort.trading.state.StateListener;
import org.anhonesteffort.trading.stats.StatsService;
import org.anhonesteffort.trading.strategy.MetaStrategy;
import org.anhonesteffort.trading.strategy.impl.SimpleStrategyFactory;
import org.anhonesteffort.trading.ws.WsService;
import org.anhonesteffort.trading.state.MatchingStateCurator;
import org.anhonesteffort.trading.state.GdaxEvent;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class TradeBoi {

  private final TradeBoiConfig config;
  private final HttpClientWrapper http;

  public TradeBoi() throws IOException, NoSuchAlgorithmException {
    config = new TradeBoiConfig();
    http   = new HttpClientWrapper(config);
  }

  private EventHandler<GdaxEvent> handlerFor(StateListener... listeners) {
    return new MatchingStateCurator(
        new CompatLimitOrderBook(),
        new HashSet<>(Arrays.asList(listeners))
    );
  }

  private void run() throws Exception {
    List<EventHandler> processors = new LinkedList<>();
    StatsService       statistics = new StatsService(config);
    DslContext         dslContext = null;

    if (config.getTradingEnabled()) {
      processors.add(handlerFor(
          new MetaStrategy(new SimpleStrategyFactory(http))
      ));
    }

    if (config.getReplEnabled()) {
      processors.add(handlerFor(dslContext = new DslContext()));
    }

    if (config.getStatsEnabled()) {
      processors.add(handlerFor(statistics.listener()));
    }

    if (processors.isEmpty()) {
      throw new RuntimeException("you gotta enable something, dude");
    }

    DisruptorService disruptor = new DisruptorService(
        config, new BlockingWaitStrategy(), processors.toArray(new EventHandler[processors.size()])
    );

    WsService wsService = new WsService(config, disruptor.ringBuffer(), http);

    if (config.getStatsEnabled()) { statistics.start(); }
    disruptor.start();
    wsService.start();

    new ShutdownProcedure(http, Optional.ofNullable(dslContext), statistics, disruptor, wsService).call();
  }

  public static void main(String[] args) throws Exception {
    new TradeBoi().run();
  }

}
