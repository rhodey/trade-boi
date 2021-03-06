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

package org.anhonesteffort.trading.stats;

import io.netty.channel.ChannelInboundHandler;
import org.anhonesteffort.trading.compute.Computation;
import org.anhonesteffort.trading.state.GdaxState;

public class StatsChannelHandlerFactory extends Computation<Void> {

  public StatsChannelHandlerFactory() {
    super(null);
  }

  public ChannelInboundHandler newHandler() {
    return new StatsChannelHandler(this);
  }

  protected void onChannelActive(StatsChannelHandler handler) {
    addChild(handler);
  }

  protected void onChannelInactive(StatsChannelHandler handler) {
    removeChild(handler);
  }

  @Override
  public Void computeNextResult(GdaxState state, long ns) {
    return null;
  }

}
