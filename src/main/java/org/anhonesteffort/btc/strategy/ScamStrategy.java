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

  private final SpreadComputation  spread        = new SpreadComputation();
  private final SummingComputation buyVolume30s  = new SummingComputation(new TakeVolumeComputation(Order.Side.BID), 1000 * 30);
  private final SummingComputation buyVolume5s   = new SummingComputation(new TakeVolumeComputation(Order.Side.BID), 1000 * 5);
  private final SummingComputation sellVolume30s = new SummingComputation(new TakeVolumeComputation(Order.Side.ASK), 1000 * 30);
  private final SummingComputation sellVolume5s  = new SummingComputation(new TakeVolumeComputation(Order.Side.ASK), 1000 * 5);
  private final LongCaster         caster;

  public ScamStrategy(LongCaster caster) {
    this.caster = caster;
    addChildren(spread, buyVolume30s, buyVolume5s, sellVolume30s, sellVolume5s);
  }

  private Optional<Boolean> isBullish30s() {
    if (!buyVolume30s.getResult().isPresent() || !sellVolume30s.getResult().isPresent()) {
      return Optional.empty();
    } else {
      return Optional.of(buyVolume30s.getResult().get() > sellVolume30s.getResult().get());
    }
  }

  private Optional<Boolean> isBearish5s() {
    if (!buyVolume5s.getResult().isPresent() || !sellVolume5s.getResult().isPresent()) {
      return Optional.empty();
    } else {
      return Optional.of(sellVolume5s.getResult().get() > buyVolume5s.getResult().get());
    }
  }

  @Override
  protected Void computeNextResult(State state, long nanoseconds) {
    Optional<Boolean> bullish30s = isBullish30s();
    Optional<Boolean> bearish5s  = isBearish5s();

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
