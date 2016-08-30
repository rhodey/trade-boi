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
package org.anhonesteffort.btc.stat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.anhonesteffort.btc.ScamConfig;
import org.anhonesteffort.btc.util.LongCaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class StatService {

  private static final Logger log = LoggerFactory.getLogger(StatService.class);
  private static final Integer SERVER_PORT = 3133;

  private final CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();

  private final ScamConfig     config;
  private final LongCaster     caster;
  private final EventLoopGroup bossGroup;
  private final EventLoopGroup workerGroup;

  private Channel channel;

  public StatService(ScamConfig config, LongCaster caster) {
    this.config = config;
    this.caster = caster;
    bossGroup   = new NioEventLoopGroup();
    workerGroup = new NioEventLoopGroup();
  }

  public CompletableFuture<Void> getShutdownFuture() {
    return shutdownFuture;
  }

  public void start() throws InterruptedException {
    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .option(ChannelOption.SO_BACKLOG, 128)
             .childOption(ChannelOption.SO_KEEPALIVE, true)
             .childOption(ChannelOption.TCP_NODELAY, true)
             .childHandler(new ChannelInitializer<SocketChannel>() {
               @Override
               public void initChannel(SocketChannel ch) {
                 ch.pipeline().addLast("encoder", null);
                 ch.pipeline().addLast("decoder", null);
                 ch.pipeline().addLast("handler", null);
               }
             });

    channel = bootstrap.bind(SERVER_PORT).sync().channel();
    channel.closeFuture().addListener(close -> {
      if (close.cause() != null) { shutdown(close.cause()); }
      else                       { shutdown(); }
    });

    log.info("bound to port " + SERVER_PORT);
  }

  public boolean shutdown() {
    if (shutdownFuture.complete(null)) {
      channel.close();
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
      return true;
    } else {
      return false;
    }
  }

  private boolean shutdown(Throwable throwable) {
    if (shutdownFuture.completeExceptionally(throwable)) {
      channel.close();
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
      return true;
    } else {
      return false;
    }
  }

}
