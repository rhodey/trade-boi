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

package org.anhonesteffort.btc.strategy;

import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.compute.SpreadComputation;
import org.anhonesteffort.btc.compute.SummingComputation;
import org.anhonesteffort.btc.compute.TakeVolumeComputation;
import org.anhonesteffort.btc.http.HttpClientWrapper;
import org.anhonesteffort.btc.state.State;
import org.anhonesteffort.btc.util.LongCaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ScamStrategy extends Strategy<Void> {

  private static final Logger log = LoggerFactory.getLogger(ScamStrategy.class);

  private static final Integer RECENT_PERIOD_MS        = 1000 * 30;
  private static final Integer NOW_PERIOD_MS           = 1000 *  5;
  private static final Double  BULLISH_THRESHOLD_BTC   = 1.50d;
  private static final Double  BULLISH_THRESHOLD_SCORE = 1.25d;

  private final SpreadComputation  spread           = new SpreadComputation();
  private final SummingComputation buyVolumeRecent  = new SummingComputation(new TakeVolumeComputation(Order.Side.BID), RECENT_PERIOD_MS);
  private final SummingComputation buyVolumeNow     = new SummingComputation(new TakeVolumeComputation(Order.Side.BID), NOW_PERIOD_MS);
  private final SummingComputation sellVolumeRecent = new SummingComputation(new TakeVolumeComputation(Order.Side.ASK), RECENT_PERIOD_MS);
  private final SummingComputation sellVolumeNow    = new SummingComputation(new TakeVolumeComputation(Order.Side.ASK), NOW_PERIOD_MS);

  private final HttpClientWrapper http;
  private final LongCaster        caster;

  private ScamState state = ScamState.WAITING;
  private PositionOpenStrategy openStrategy;
  private PositionHoldStrategy holdStrategy;
  private PositionCloseStrategy closeStrategy;

  private enum ScamState {
    WAITING, OPENING, OPEN, CLOSING, CLOSED
  }

  public ScamStrategy(HttpClientWrapper http, LongCaster caster) {
    this.http   = http;
    this.caster = caster;
    addChildren(spread, buyVolumeRecent, buyVolumeNow, sellVolumeRecent, sellVolumeNow);
  }

  private Optional<Double> getBullishScore() {
    if (!buyVolumeRecent.getResult().isPresent() || !sellVolumeRecent.getResult().isPresent()) {
      return Optional.empty();
    } else if (buyVolumeRecent.getResult().get() < 0l || sellVolumeRecent.getResult().get() < 0l) {
      throw new RuntimeException("!!! buy or sell volume sum is less than zero !!!");
    } else if (caster.toDouble(buyVolumeRecent.getResult().get()) < BULLISH_THRESHOLD_BTC) {
      return Optional.of(-1d);
    } else if (sellVolumeRecent.getResult().get() == 0l) {
      return Optional.of(Double.MAX_VALUE);
    } else {
      return Optional.of(
          ((double) buyVolumeRecent.getResult().get()) / ((double) sellVolumeRecent.getResult().get())
      );
    }
  }

  private Optional<Boolean> isNowBearish() {
    if (!buyVolumeNow.getResult().isPresent() || !sellVolumeNow.getResult().isPresent()) {
      return Optional.empty();
    } else {
      return Optional.of(buyVolumeNow.getResult().get() < sellVolumeNow.getResult().get());
    }
  }

  private boolean isBullish() {
    Optional<Double>  bullishScore = getBullishScore();
    Optional<Boolean> isNowBearish = isNowBearish();

    if (bullishScore.isPresent() && isNowBearish.isPresent()) {
      return bullishScore.get() >= BULLISH_THRESHOLD_SCORE && !isNowBearish.get();
    } else {
      return false;
    }
  }

  private void handleWaiting(State state) {
    if (isBullish() && caster.toDouble(spread.getResult().get()) > 0.01d) {
      double bidFloor   = caster.toDouble(state.getOrderBook().getBidLimits().peek().get().getPrice());
      double askCeiling = caster.toDouble(state.getOrderBook().getAskLimits().peek().get().getPrice());
      double bidPrice   = askCeiling - 0.01d;

      log.info("wanna open ask position at " + bidPrice + " & hope bid floor raises from " + bidFloor);
      openStrategy = new PositionOpenStrategy(http);
      addChildren(openStrategy);
      this.state = ScamState.OPENING;
    }
  }

  @Override
  protected Void computeNextResult(State state, long nanoseconds) {
    switch (this.state) {
      case WAITING:
        handleWaiting(state);
        break;

      case OPENING:
        if (openStrategy.getResult()) {
          log.info("position opened!");
          holdStrategy = new PositionHoldStrategy(http);
          removeChildren(openStrategy);
          addChildren(holdStrategy);
          this.state = ScamState.OPEN;
        }
        break;

      case OPEN:
        if (holdStrategy.getResult()) {
          log.info("position held!");
          closeStrategy = new PositionCloseStrategy(http);
          removeChildren(holdStrategy);
          addChildren(closeStrategy);
          this.state = ScamState.CLOSING;
        }
        break;

      case CLOSING:
        if (closeStrategy.getResult()) {
          log.info("position closed!");
          removeChildren(closeStrategy);
          this.state = ScamState.CLOSED;
        }
        break;
    }

    return null;
  }

  @Override
  public void onStateReset() {
    super.onStateReset();
    log.info("on results invalidated");
  }

}
