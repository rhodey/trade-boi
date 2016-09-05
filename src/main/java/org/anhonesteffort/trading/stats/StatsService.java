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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import org.anhonesteffort.trading.TradeBoiConfig;
import org.anhonesteffort.trading.Service;
import org.anhonesteffort.trading.state.StateListener;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class StatsService implements Service {

  private final CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();
  private final StatsChannelHandlerFactory handlers = new StatsChannelHandlerFactory();
  private final TradeBoiConfig config;

  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;
  private Channel        channel;

  public StatsService(TradeBoiConfig config) {
    this.config = config;
  }

  public StateListener[] listeners() {
    return new StateListener[] { handlers };
  }

  @Override
  public CompletableFuture<Void> shutdownFuture() {
    return shutdownFuture;
  }

  @Override
  public void start() throws InterruptedException {
    ServerBootstrap bootstrap   = new ServerBootstrap();
                    bossGroup   = new NioEventLoopGroup();
                    workerGroup = new NioEventLoopGroup();

    bootstrap.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childOption(ChannelOption.SO_KEEPALIVE, true)
             .childOption(ChannelOption.TCP_NODELAY, true)
             .childHandler(new ChannelInitializer<SocketChannel>() {
               @Override
               public void initChannel(SocketChannel ch) {
                 ch.pipeline().addLast("frameEncoder", new LengthFieldPrepender(4));
                 ch.pipeline().addLast("msgEncoder", new ProtobufEncoder());
                 ch.pipeline().addLast("handler", handlers.newHandler());
               }
             });

    channel = bootstrap.bind(config.getStatsPort()).sync().channel();
    channel.closeFuture().addListener(close -> {
      if (close.cause() != null) { shutdown(close.cause()); }
      else                       { shutdown(new IOException("channel closed unexpectedly")); }
    });
  }

  @Override
  public boolean shutdown() {
    if (channel != null && shutdownFuture.complete(null)) {
      channel.close();
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
      return true;
    } else {
      return false;
    }
  }

  private boolean shutdown(Throwable throwable) {
    if (channel != null && shutdownFuture.completeExceptionally(throwable)) {
      channel.close();
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
      return true;
    } else {
      return false;
    }
  }

}
