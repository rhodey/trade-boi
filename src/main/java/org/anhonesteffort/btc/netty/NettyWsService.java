/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.anhonesteffort.btc.netty;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.anhonesteffort.btc.http.HttpClientWrapper;
import org.anhonesteffort.btc.state.OrderEvent;
import org.anhonesteffort.btc.util.LongCaster;
import org.anhonesteffort.btc.ws.WsMessageSorter;
import org.anhonesteffort.btc.ws.WsOrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class NettyWsService implements ExceptionHandler<OrderEvent>, EventFactory<OrderEvent> {

  private static final Logger log = LoggerFactory.getLogger(NettyWsService.class);

  private static final String  WS_HOST            = "ws-feed.exchange.coinbase.com";
  private static final String  WS_URI             = "wss://" + WS_HOST;
  private static final Integer WS_PORT            = 443;
  private static final Integer CONNECT_TIMEOUT_MS = 5000;
  private static final Integer READ_TIMEOUT_MS    = 5000;

  private final CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();
  private final HttpClientWrapper       http           = new HttpClientWrapper();

  private final Disruptor<OrderEvent>      wsDisruptor;
  private final EventHandler<OrderEvent>[] handlers;
  private final LongCaster                 caster;

  private Channel channel;

  public NettyWsService(
      WaitStrategy waitStrategy, int bufferSize, EventHandler<OrderEvent>[] handlers, LongCaster caster
  ) {
    this.handlers = handlers;
    this.caster   = caster;
    wsDisruptor   = new Disruptor<>(
        this, bufferSize, new DisruptorThreadFactory(), ProducerType.SINGLE, waitStrategy
    );
  }

  public CompletableFuture<Void> getShutdownFuture() {
    return shutdownFuture;
  }

  @SuppressWarnings("unchecked")
  public void start() throws URISyntaxException, SSLException {
    Bootstrap             bootstrap = new Bootstrap();
    EventLoopGroup        eventLoop = new NioEventLoopGroup();
    WsOrderEventPublisher publisher = new WsOrderEventPublisher(wsDisruptor.getRingBuffer(), caster);
    WsMessageSorter       sorter    = new WsMessageSorter(publisher, http);

    final SslContext                sslContext = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
    final NettyWsMessageReceiver    wsReceiver = new NettyWsMessageReceiver(sorter);
    final WebSocketClientHandshaker handshake  = WebSocketClientHandshakerFactory.newHandshaker(
        new URI(WS_URI), WebSocketVersion.V13, null, true, new DefaultHttpHeaders()
    );

    bootstrap.group(eventLoop)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
             .handler(new ChannelInitializer<SocketChannel>() {
               @Override
               protected void initChannel(SocketChannel channel) {
                 channel.pipeline().addLast(new ReadTimeoutHandler(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS));
                 channel.pipeline().addLast(sslContext.newHandler(channel.alloc(), WS_HOST, WS_PORT));
                 channel.pipeline().addLast(new HttpClientCodec());
                 channel.pipeline().addLast(new HttpObjectAggregator(8192));
                 channel.pipeline().addLast(new WebSocketClientProtocolHandler(handshake, false));
                 channel.pipeline().addLast(wsReceiver);
               }
             });

    wsDisruptor.handleEventsWith(handlers);
    wsDisruptor.setDefaultExceptionHandler(this);
    wsDisruptor.start();

    channel = bootstrap.connect(WS_HOST, WS_PORT).channel();
    channel.closeFuture().addListener(close -> shutdown());
  }

  public boolean shutdown() {
    if (shutdownFuture.complete(null)) {
      channel.close();
      http.shutdown();
      return true;
    } else {
      return false;
    }
  }

  private boolean shutdown(Throwable throwable) {
    if (shutdownFuture.completeExceptionally(throwable)) {
      channel.close();
      http.shutdown();
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
