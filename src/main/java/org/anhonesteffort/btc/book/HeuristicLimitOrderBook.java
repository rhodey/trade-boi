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

package org.anhonesteffort.btc.book;

import java.util.Optional;

public class HeuristicLimitOrderBook extends LimitOrderBook {

  public Optional<Long> getSpread() {
    Optional<Limit> bestAsk = askLimits.peek();
    Optional<Limit> bestBid = bidLimits.peek();

    if (bestAsk.isPresent() && bestBid.isPresent()) {
      return Optional.of(bestAsk.get().getPrice() - bestBid.get().getPrice());
    } else {
      return Optional.empty();
    }
  }

}
