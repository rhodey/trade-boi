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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

public class NettyWsService {

  private static final String WS_ENDPOINT = "wss://ws-feed.exchange.coinbase.com";

  private final CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();
  private final SslContext ssl;
  private final URI uri;

  public NettyWsService() throws URISyntaxException, SSLException {
    ssl = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);
    uri = new URI(WS_ENDPOINT);
  }

  public CompletableFuture<Void> getShutdownFuture() {
    return shutdownFuture;
  }

  public void start() throws InterruptedException {
    EventLoopGroup         group       = new NioEventLoopGroup();
    Bootstrap              bootstrap   = new Bootstrap();
    NettyWsMessageReceiver wsMessageRx = new NettyWsMessageReceiver(uri);

    bootstrap.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
               @Override
               protected void initChannel(SocketChannel channel) {
                 ChannelPipeline pipeline = channel.pipeline();
                 pipeline.addLast(ssl.newHandler(channel.alloc(), uri.getHost(), 443));
                 pipeline.addLast(new HttpClientCodec(), new HttpObjectAggregator(8192), wsMessageRx);
               }
             });

    bootstrap.connect(uri.getHost(), 443).sync()
             .channel().closeFuture().sync();
  }

  public static void main(String[] args) throws Exception {
    new NettyWsService().start();
  }

}
