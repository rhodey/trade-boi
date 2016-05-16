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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.lmax.disruptor.RingBuffer;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;
import org.anhonesteffort.btc.ws.message.Message;
import org.anhonesteffort.btc.ws.message.MessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class WsMessageReceiver implements WebSocketListener, FutureCallback<Void> {

  private static final Logger log = LoggerFactory.getLogger(WsMessageReceiver.class);

  private final SettableFuture<Void> errorFuture = SettableFuture.create();
  private final RingBuffer<Message>  ringBuffer;
  private final MessageDecoder       decoder;
  private final WsSubscribeHelper    helper;

  public WsMessageReceiver(RingBuffer<Message> ringBuffer,
                           MessageDecoder      decoder,
                           WsSubscribeHelper   helper)
  {
    this.ringBuffer = ringBuffer;
    this.decoder    = decoder;
    this.helper     = helper;
  }

  public ListenableFuture<Void> getErrorFuture() {
    return errorFuture;
  }

  @Override
  public void onOpen(WebSocket socket, Response response) {
    log.info("connection opened");
    Futures.addCallback(helper.subscribe(socket), this);
  }

  @Override
  public void onSuccess(Void aVoid) {
    log.info("subscribed to market feed");
  }

  @Override
  public void onMessage(ResponseBody body) {
    try {

      long sequence = ringBuffer.next();
      decoder.decode(body, ringBuffer.get(sequence));
      ringBuffer.publish(sequence);

    } catch (Throwable e) {
      errorFuture.setException(e);
    } finally {
      body.close();
    }
  }

  @Override
  public void onFailure(IOException e, Response response) {
    errorFuture.setException(e);
  }

  @Override
  public void onFailure(Throwable e) {
    errorFuture.setException(e);
  }

  @Override
  public void onClose(int code, String reason) {
    errorFuture.setException(new WsException(
        "websocket closed with code " + code + " and reason -> " + reason
    ));
  }

  @Override
  public void onPong(Buffer pong) {
    log.debug("pong received");
  }

}
