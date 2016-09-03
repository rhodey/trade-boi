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
package org.anhonesteffort.btc.ws;

import com.lmax.disruptor.RingBuffer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.anhonesteffort.btc.ScamConfig;
import org.anhonesteffort.btc.Service;
import org.anhonesteffort.btc.http.HttpClientWrapper;
import org.anhonesteffort.btc.state.GdaxEvent;
import org.anhonesteffort.trading.util.LongCaster;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class WsService implements Service {

  private static final String  PROD_WS_HOST    = "ws-feed.exchange.coinbase.com";
  private static final String  SANDBOX_WS_HOST = "ws-feed-public.sandbox.gdax.com";
  private static final Integer WS_PORT         = 443;

  private final CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();
  private final ScamConfig config;
  private final WsMessageSorter messageSorter;

  private Channel channel;

  public WsService(
      ScamConfig config, RingBuffer<GdaxEvent> ringBuffer,
      HttpClientWrapper http, LongCaster caster
  ) {
    this.config   = config;
    messageSorter = new WsMessageSorter(
        new WsRingPublisher(ringBuffer, caster), http
    );
  }

  @Override
  public CompletableFuture<Void> shutdownFuture() {
    return shutdownFuture;
  }

  @Override
  public void start() throws URISyntaxException, SSLException {
    final Bootstrap                 bootstrap       = new Bootstrap();
    final SslContext                sslContext      = SslContextBuilder.forClient().build();
    final WsMessageReceiver         messageReceiver = new WsMessageReceiver(messageSorter);
    final WebSocketClientHandshaker wsHandshake     = WebSocketClientHandshakerFactory.newHandshaker(
        new URI("wss://" + (config.getGdaxSandbox() ? SANDBOX_WS_HOST : PROD_WS_HOST)),
        WebSocketVersion.V13, null, true, new DefaultHttpHeaders()
    );

    bootstrap.group(new NioEventLoopGroup())
             .channel(NioSocketChannel.class)
             .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getWsConnectTimeoutMs())
             .handler(new ChannelInitializer<SocketChannel>() {
               @Override
               protected void initChannel(SocketChannel channel) {
                 channel.pipeline().addLast(new ReadTimeoutHandler(config.getWsReadTimeoutMs(), TimeUnit.MILLISECONDS));
                 channel.pipeline().addLast(sslContext.newHandler(channel.alloc(), PROD_WS_HOST, WS_PORT));
                 channel.pipeline().addLast(new HttpClientCodec());
                 channel.pipeline().addLast(new HttpObjectAggregator(8192));
                 channel.pipeline().addLast(new WsClientProtocolHandler(wsHandshake));
                 channel.pipeline().addLast(messageReceiver);
               }
             });

    channel = bootstrap.connect(PROD_WS_HOST, WS_PORT).channel();
    channel.closeFuture().addListener(close -> {
      if (close.cause() != null) { shutdown(close.cause()); }
      else                       { shutdown(new IOException("channel closed unexpectedly")); }
    });
  }

  @Override
  public boolean shutdown() {
    if (shutdownFuture.complete(null)) {
      channel.close();
      return true;
    } else {
      return false;
    }
  }

  private boolean shutdown(Throwable throwable) {
    if (shutdownFuture.completeExceptionally(throwable)) {
      channel.close();
      return true;
    } else {
      return false;
    }
  }

}
