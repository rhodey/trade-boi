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
import org.anhonesteffort.btc.disruptor.DisruptorService;
import org.anhonesteffort.btc.http.HttpClientWrapper;
import org.anhonesteffort.btc.persist.PersistService;
import org.anhonesteffort.btc.state.StateListener;
import org.anhonesteffort.btc.stats.StatsService;
import org.anhonesteffort.btc.strategy.Strategy;
import org.anhonesteffort.btc.strategy.StrategyFactory;
import org.anhonesteffort.btc.strategy.impl.SimpleStrategyFactory;
import org.anhonesteffort.btc.ws.WsService;
import org.anhonesteffort.btc.state.MatchingStateCurator;
import org.anhonesteffort.btc.state.GdaxEvent;
import org.anhonesteffort.btc.strategy.MetaStrategy;
import org.anhonesteffort.trading.book.LimitOrderBook;
import org.anhonesteffort.trading.util.LongCaster;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Scam {

  private final ScamConfig config;
  private final LongCaster caster;
  private final HttpClientWrapper http;

  public Scam() throws IOException, NoSuchAlgorithmException {
    config = new ScamConfig();
    caster = new LongCaster(config.getPrecision(), config.getAccuracy());
    http   = new HttpClientWrapper(config);
  }

  private EventHandler<GdaxEvent> handlerFor(StateListener... listeners) {
    return new MatchingStateCurator(
        new LimitOrderBook(config.getLimitInitSize()),
        new HashSet<>(Arrays.asList(listeners))
    );
  }

  private EventHandler[] handlersForConfig(Strategy strategy, PersistService persist, StatsService stats) {
    List<EventHandler> handlerList = new LinkedList<>();

    if (config.getTradingEnabled()) {
      handlerList.add(handlerFor(strategy));
    }
    if (config.getPersistenceEnabled()) {
      handlerList.add(handlerFor(persist.listeners()));
    }
    if (config.getStatsEnabled()) {
      handlerList.add(handlerFor(stats.listeners()));
    }

    if (handlerList.isEmpty()) {
      throw new RuntimeException("you gotta enable something, dude");
    } else {
      return handlerList.toArray(new EventHandler[handlerList.size()]);
    }
  }

  public void run() throws Exception {
    StrategyFactory strategies   = new SimpleStrategyFactory(http, caster);
    Strategy        metaStrategy = new MetaStrategy(strategies);
    PersistService  persistence  = new PersistService(config);
    StatsService    statistics   = new StatsService(config);

    DisruptorService disruptor = new DisruptorService(
        config, new BlockingWaitStrategy(), handlersForConfig(metaStrategy, persistence, statistics)
    );

    WsService wsService = new WsService(config, disruptor.ringBuffer(), http, caster);

    if (config.getPersistenceEnabled()) { persistence.start(); }
    if (config.getStatsEnabled()) { statistics.start(); }
    disruptor.start();
    wsService.start();

    new ShutdownProcedure(
        http, persistence, statistics, disruptor, wsService
    ).call();
  }

  public static void main(String[] args) throws Exception {
    new Scam().run();
  }

}
