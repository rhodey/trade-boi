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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.anhonesteffort.trading.proto.TradingProtoFactory;
import org.anhonesteffort.trading.state.Events;
import org.anhonesteffort.trading.state.GdaxState;
import org.anhonesteffort.trading.state.StateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class StatsChannelHandler extends ChannelInboundHandlerAdapter implements StateListener {

  private static final Logger log = LoggerFactory.getLogger(StatsChannelHandler.class);

  private final TradingProtoFactory proto = new TradingProtoFactory();
  private final StatsChannelHandlerFactory parent;
  private Optional<ChannelHandlerContext> context = Optional.empty();

  public StatsChannelHandler(StatsChannelHandlerFactory parent) {
    this.parent = parent;
  }

  @Override
  public void channelActive(ChannelHandlerContext context) {
    this.context = Optional.of(context);
    parent.onChannelActive(this);
  }

  @Override
  public void onStateChange(GdaxState state, long nanoseconds) {
    if (context.isPresent() && state.event().isDefined()) {
      context.get().writeAndFlush(proto.orderEvent(state.event().get()));
    }
  }

  @Override
  public void onStateSyncStart(long nanoseconds) {
    context.ifPresent(ctx ->
      ctx.writeAndFlush(proto.orderEvent(Events.syncStart(nanoseconds)))
    );
  }

  @Override
  public void onStateSyncEnd(long nanoseconds) {
    context.ifPresent(ctx ->
        ctx.writeAndFlush(proto.orderEvent(Events.syncEnd(nanoseconds)))
    );
  }

  @Override
  public void channelRead(ChannelHandlerContext context, Object msg) {
    log.warn("received unexpected message from client, closing");
    context.writeAndFlush(proto.error("don't talk to me.")).addListener(future -> context.close());
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
    log.error("caught unexpected exception, closing", cause);
    context.writeAndFlush(proto.error("what did you do?")).addListener(future -> context.close());
  }

  @Override
  public void channelInactive(ChannelHandlerContext context) {
    parent.onChannelInactive(this);
  }

}