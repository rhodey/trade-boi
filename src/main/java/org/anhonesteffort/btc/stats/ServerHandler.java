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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.anhonesteffort.btc.state.State;
import org.anhonesteffort.btc.state.StateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHandler extends ChannelInboundHandlerAdapter implements StateListener {

  private static final Logger log = LoggerFactory.getLogger(ServerHandler.class);
  private final StatsProtoFactory proto = new StatsProtoFactory();

  @Override
  public void channelActive(ChannelHandlerContext context) {
    context.writeAndFlush(proto.error("lol idk"));
  }

  @Override
  public void onStateChange(State state, long nanoseconds) { }

  @Override
  public void onStateReset() { }

  @Override
  public void channelRead(ChannelHandlerContext context, Object request) {
    log.warn("received unexpected message from client, closing");
    context.close();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
    log.error("caught unexpected exception, closing", cause);
    context.close();
  }

}