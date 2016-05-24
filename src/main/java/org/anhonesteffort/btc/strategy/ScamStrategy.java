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
import org.anhonesteffort.btc.state.State;
import org.anhonesteffort.btc.util.LongCaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ScamStrategy extends Strategy<Void> {

  private static final Logger log = LoggerFactory.getLogger(ScamStrategy.class);

  private static final Integer RECENT_PERIOD_MS = 1000 * 30;
  private static final Integer NOW_PERIOD_MS    = 1000 *  5;

  private static final Double BULLISH_THRESHOLD_FACTOR = 1.25d;
  private static final Double BULLISH_THRESHOLD_BTC    = 1.50d;

  private final SpreadComputation  spread           = new SpreadComputation();
  private final SummingComputation buyVolumeRecent  = new SummingComputation(new TakeVolumeComputation(Order.Side.BID), RECENT_PERIOD_MS);
  private final SummingComputation buyVolumeNow     = new SummingComputation(new TakeVolumeComputation(Order.Side.BID), NOW_PERIOD_MS);
  private final SummingComputation sellVolumeRecent = new SummingComputation(new TakeVolumeComputation(Order.Side.ASK), RECENT_PERIOD_MS);
  private final SummingComputation sellVolumeNow    = new SummingComputation(new TakeVolumeComputation(Order.Side.ASK), NOW_PERIOD_MS);
  private final LongCaster         caster;

  public ScamStrategy(LongCaster caster) {
    this.caster = caster;
    addChildren(spread, buyVolumeRecent, buyVolumeNow, sellVolumeRecent, sellVolumeNow);
  }

  private Optional<Boolean> hasBeenBullish() {
    if (!buyVolumeRecent.getResult().isPresent() || !sellVolumeRecent.getResult().isPresent()) {
      return Optional.empty();
    } else {
      long buyVolRecent  = buyVolumeRecent.getResult().get();
      long sellVolRecent = sellVolumeRecent.getResult().get();

      return Optional.of(
          (buyVolRecent >= (sellVolRecent * BULLISH_THRESHOLD_FACTOR)) &&
          (caster.toDouble(buyVolRecent) > BULLISH_THRESHOLD_BTC)
      );
    }
  }

  private Optional<Boolean> isNowBearish() {
    if (!buyVolumeNow.getResult().isPresent() || !sellVolumeNow.getResult().isPresent()) {
      return Optional.empty();
    } else {
      return Optional.of(buyVolumeNow.getResult().get() <= sellVolumeNow.getResult().get());
    }
  }

  @Override
  protected Void computeNextResult(State state, long nanoseconds) {
    Optional<Boolean> bullish30s = hasBeenBullish();
    Optional<Boolean> bearish5s  = isNowBearish();

    if (bullish30s.isPresent() && bearish5s.isPresent() && bullish30s.get() && !bearish5s.get()) {
      log.info("market bullish for 30s and non-bearish in the past 5s");
      return null;
    } else {
      return null;
    }
  }

  @Override
  public void onStateReset() {
    super.onStateReset();
    log.info("on results invalidated");
  }

}
