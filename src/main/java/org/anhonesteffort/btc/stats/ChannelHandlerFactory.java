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

package org.anhonesteffort.btc.stats;

import io.netty.channel.ChannelInboundHandler;
import org.anhonesteffort.btc.state.State;
import org.anhonesteffort.btc.state.StateListener;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelHandlerFactory implements StateListener {

  private final Set<ServerHandler> handlers = Collections.newSetFromMap(new ConcurrentHashMap<>());

  public ChannelInboundHandler newHandler() {
    return new ServerHandler(this);
  }

  protected void onChannelActive(ServerHandler handler) {
    handlers.add(handler);
  }

  protected void onChannelInactive(ServerHandler handler) {
    handlers.remove(handler);
  }

  @Override
  public void onStateChange(State state, long nanoseconds) {
    for (ServerHandler handler : handlers) { handler.onStateChange(state, nanoseconds); }
  }

  @Override
  public void onStateReset() {
    handlers.forEach(ServerHandler::onStateReset);
  }

}
