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

import com.lmax.disruptor.EventHandler;
import org.anhonesteffort.btc.http.HttpClientWrapper;
import org.anhonesteffort.btc.http.response.OrderBookResponse;
import org.anhonesteffort.btc.ws.message.ErrorAccessor;
import org.anhonesteffort.btc.ws.message.MarketAccessor;
import org.anhonesteffort.btc.ws.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class WsMessageProcessor implements EventHandler<Message> {

  private static final Logger log = LoggerFactory.getLogger(WsMessageProcessor.class);

  private final ErrorAccessor     error  = new ErrorAccessor();
  private final MarketAccessor    market = new MarketAccessor();
  private final HttpClientWrapper http;

  private Optional<Long> messageSeqLast = Optional.empty();

  public WsMessageProcessor(HttpClientWrapper http) {
    this.http = http;
  }

  private void process(Message message) {
    log.info("process -> " + market.getSequence(message));
    // todo: publish to the order book ring buffer
  }

  private void rebuildOrderBook() throws InterruptedException, ExecutionException {
    log.info("asking coinbase for the order book...");
    OrderBookResponse orderBook = http.geOrderBook().get();
    log.info("received the order book, seq -> " + orderBook.getSequence());

    log.info("___asks___");
    for (int i = 0; i < 15; i++) {
      log.info(orderBook.getAsks().remove().toString());
    }

    log.info("___bids___");
    for (int i = 0; i < 15; i++) {
      log.info(orderBook.getBids().remove().toString());
    }

    messageSeqLast = Optional.of(orderBook.getSequence());
    // todo: publish RESET message to the order book ring buffer
  }

  public void checkSeqAndProcess(Message message) throws InterruptedException, ExecutionException {
    long messageSeq = market.getSequence(message);

    if (!messageSeqLast.isPresent() || messageSeq == (messageSeqLast.get() + 1)) {
      messageSeqLast = Optional.of(messageSeq);
      process(message);
    } else if (messageSeq > messageSeqLast.get()) {
      log.warn("received out of order seq -> " + messageSeq + ", expected -> " + (messageSeqLast.get() + 1));
      rebuildOrderBook();
    } else {
      log.warn("received duplicate sequence -> " + messageSeq + ", ignoring");
    }
  }

  @Override
  public void onEvent(Message message, long sequence, boolean endOfBatch) throws Exception {
    switch (message.getType()) {
      case Message.TYPE_RECEIVED:
      case Message.TYPE_OPEN:
      case Message.TYPE_DONE:
      case Message.TYPE_MATCH:
      case Message.TYPE_CHANGE:
        checkSeqAndProcess(message);
        break;

      case Message.TYPE_ERROR:
        throw new WsException("received error message: " + error.getMessage(message));

      default:
        throw new WsException("unknown message type: " + message.getType());
    }
  }

}
