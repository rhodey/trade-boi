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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class Strategy implements ComputeCallback {

  private final Set<Computation> computations = new HashSet<>();
  private int callbackCount = 0;

  protected Strategy() { }
  protected Strategy(Computation ... compute) {
    addComputations(compute);
  }

  public Set<Computation> getComputations() {
    return computations;
  }

  protected void addComputations(Computation ... compute) {
    Arrays.asList(compute).forEach(comp -> {
      comp.setCallback(this);
      computations.add(comp);
    });
  }

  protected abstract void onResultsReady();

  protected abstract void onResultsInvalidated();

  @Override
  public void onNextResult() {
    callbackCount++;
    if (callbackCount == computations.size()) {
      callbackCount = 0;
      onResultsReady();
    }
  }

  @Override
  public void onResultInvalidated() {
    callbackCount++;
    if (callbackCount == computations.size()) {
      callbackCount = 0;
      onResultsInvalidated();
    }
  }

}
