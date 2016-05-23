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

import org.anhonesteffort.btc.compute.Computation;
import org.anhonesteffort.btc.compute.ComputeCallback;
import org.anhonesteffort.btc.compute.SpreadComputation;
import org.anhonesteffort.btc.util.LongCaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class SpreadStrategy implements Strategy, ComputeCallback<Optional<Long>> {

  private static final Logger log = LoggerFactory.getLogger(SpreadStrategy.class);

  private final SpreadComputation compute = new SpreadComputation();
  private final LongCaster caster;
  private Optional<Long> lastSpread = Optional.empty();

  public SpreadStrategy(LongCaster caster) {
    this.caster = caster;
    compute.setCallback(this);
  }

  @Override
  public Computation[] getComputations() {
    return new Computation[] { compute };
  }

  @Override
  public void onNextResult(Optional<Long> spread, long nanosecods) {
    if (!spread.isPresent()) {
      lastSpread = spread;
      log.warn("!!! no spread available !!!");
    } else if (!lastSpread.isPresent()) {
      lastSpread = spread;
      log.info("new spread -> " + caster.toDouble(spread.get()));
    } else if (!spread.get().equals(lastSpread.get())) {
      lastSpread = spread;
      log.info("new spread -> " + caster.toDouble(spread.get()));
    }
  }

  @Override
  public void onResultsInvalidated() {
    lastSpread = Optional.empty();
  }

}
