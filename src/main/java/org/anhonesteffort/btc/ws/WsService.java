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

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
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
import org.anhonesteffort.btc.http.HttpClientWrapper;
import org.anhonesteffort.btc.state.OrderEvent;
import org.anhonesteffort.btc.util.LongCaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class WsService implements ExceptionHandler<OrderEvent>, EventFactory<OrderEvent> {

  private static final Logger log = LoggerFactory.getLogger(WsService.class);

  private static final String  WS_HOST = "ws-feed.exchange.coinbase.com";
  private static final String  WS_URI  = "wss://" + WS_HOST;
  private static final Integer WS_PORT = 443;

  private final CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();

  private final ScamConfig                 config;
  private final Disruptor<OrderEvent>      wsDisruptor;
  private final EventHandler<OrderEvent>[] handlers;
  private final HttpClientWrapper          http;
  private final LongCaster                 caster;

  private Channel channel;

  public WsService(
      ScamConfig config, EventHandler<OrderEvent>[] handlers,
      HttpClientWrapper http, LongCaster caster
  ) {
    this.config   = config;
    this.handlers = handlers;
    this.http     = http;
    this.caster   = caster;
    wsDisruptor   = new Disruptor<>(
        this, config.getWsBufferSize(), new DisruptorThreadFactory(),
        ProducerType.SINGLE, config.getWaitStrategy()
    );
  }

  public CompletableFuture<Void> getShutdownFuture() {
    return shutdownFuture;
  }

  @SuppressWarnings("unchecked")
  public void start() throws URISyntaxException, SSLException {
    Bootstrap       bootstrap     = new Bootstrap();
    WsRingPublisher ringPublisher = new WsRingPublisher(wsDisruptor.getRingBuffer(), caster);
    WsMessageSorter messageSorter = new WsMessageSorter(ringPublisher, http);

    final SslContext                sslContext      = SslContextBuilder.forClient().build();
    final WsMessageReceiver         messageReceiver = new WsMessageReceiver(messageSorter);
    final WebSocketClientHandshaker wsHandshake     = WebSocketClientHandshakerFactory.newHandshaker(
        new URI(WS_URI), WebSocketVersion.V13, null, true, new DefaultHttpHeaders()
    );

    bootstrap.group(new NioEventLoopGroup())
             .channel(NioSocketChannel.class)
             .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getWsConnectTimeoutMs())
             .handler(new ChannelInitializer<SocketChannel>() {
               @Override
               protected void initChannel(SocketChannel channel) {
                 channel.pipeline().addLast(new ReadTimeoutHandler(config.getWsReadTimeoutMs(), TimeUnit.MILLISECONDS));
                 channel.pipeline().addLast(sslContext.newHandler(channel.alloc(), WS_HOST, WS_PORT));
                 channel.pipeline().addLast(new HttpClientCodec());
                 channel.pipeline().addLast(new HttpObjectAggregator(8192));
                 channel.pipeline().addLast(new WsClientProtocolHandler(wsHandshake));
                 channel.pipeline().addLast(messageReceiver);
               }
             });

    wsDisruptor.handleEventsWith(handlers);
    wsDisruptor.setDefaultExceptionHandler(this);
    wsDisruptor.start();

    channel = bootstrap.connect(WS_HOST, WS_PORT).channel();
    channel.closeFuture().addListener(close -> {
      if (close.cause() != null) { shutdown(close.cause()); }
      else                       { shutdown(); }
    });
  }

  public boolean shutdown() {
    if (shutdownFuture.complete(null)) {
      channel.close();
      http.close();
      return true;
    } else {
      return false;
    }
  }

  private boolean shutdown(Throwable throwable) {
    if (shutdownFuture.completeExceptionally(throwable)) {
      channel.close();
      http.close();
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void handleOnStartException(Throwable throwable) {
    if (shutdown(throwable)) {
      log.error("error starting disruptor", throwable);
    }
  }

  @Override
  public void handleEventException(Throwable throwable, long sequence, OrderEvent event) {
    if (shutdown(throwable)) {
      log.error("error processing disruptor event", throwable);
    }
  }

  @Override
  public void handleOnShutdownException(Throwable throwable) {
    if (shutdown(throwable)) {
      log.error("error shutting down disruptor", throwable);
    }
  }

  private static class DisruptorThreadFactory implements ThreadFactory {
    private int count = 0;

    @Override
    public Thread newThread(Runnable runnable) {
      Thread thread = new Thread(runnable, "ws-disrupt-" + (count++));
      thread.setDaemon(true);
      return thread;
    }
  }

  @Override
  public OrderEvent newInstance() {
    return new OrderEvent();
  }

}
