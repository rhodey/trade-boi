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
import org.anhonesteffort.btc.util.LongCaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScamStrategy extends Strategy {

  private static final Logger log = LoggerFactory.getLogger(ScamStrategy.class);

  private final SpreadComputation  spread           = new SpreadComputation();
  private final SummingComputation minuteBuyVolume  = new SummingComputation(new TakeVolumeComputation(Order.Side.BID), 1000 * 10);
  private final SummingComputation minuteSellVolume = new SummingComputation(new TakeVolumeComputation(Order.Side.ASK), 1000 * 10);

  private final LongCaster caster;

  public ScamStrategy(LongCaster caster) {
    this.caster = caster;
    addComputations(spread, minuteBuyVolume, minuteSellVolume);
  }

  @Override
  protected void onResultsReady() {
    if (spread.getResult().isPresent()) {
      log.info("spread -> " + spread.getResult().get());
    }

    if (minuteBuyVolume.getResult().isPresent()) {
      log.info("minute buy volume -> " + minuteBuyVolume.getResult().get());
    }

    if (minuteSellVolume.getResult().isPresent()) {
      log.info("minute sell volume -> " + minuteSellVolume.getResult().get());
    }
  }

  @Override
  protected void onResultsInvalidated() {
    log.info("on results invalidated");
  }

}
