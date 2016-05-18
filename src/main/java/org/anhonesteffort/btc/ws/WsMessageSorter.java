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

import com.fasterxml.jackson.databind.JsonNode;
import org.anhonesteffort.btc.http.HttpClientWrapper;
import org.anhonesteffort.btc.http.response.OrderBookResponse;
import org.anhonesteffort.btc.ws.message.Accessor;
import org.anhonesteffort.btc.ws.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class WsMessageSorter {

  private static final Logger log = LoggerFactory.getLogger(WsMessageSorter.class);

  private final Accessor accessor = new Accessor();
  private final WsOrderEventPublisher publisher;
  private final HttpClientWrapper http;

  private Optional<Long> messageSeqLast = Optional.empty();

  public WsMessageSorter(WsOrderEventPublisher publisher, HttpClientWrapper http) {
    this.publisher = publisher;
    this.http      = http;
  }

  private void checkSeqAndPublish(JsonNode root, String type, long sequence) throws WsException, InterruptedException, ExecutionException {
    if (!messageSeqLast.isPresent() || sequence == (messageSeqLast.get() + 1)) {
      messageSeqLast = Optional.of(sequence);
      publisher.publishMessage(root, type);
    } else if (sequence > messageSeqLast.get()) {
      log.warn("received out of order seq -> " + sequence + ", expected -> " + (messageSeqLast.get() + 1));
      OrderBookResponse orderBook = http.geOrderBook().get();
      messageSeqLast = Optional.of(orderBook.getSequence());
      publisher.publishBook(orderBook);
    } else {
      log.warn("received duplicate sequence -> " + sequence + ", ignoring");
    }
  }

  public void sort(JsonNode root) throws WsException, InterruptedException, ExecutionException {
    String type = accessor.getType(root);
    switch (type) {
      case Message.TYPE_RECEIVED:
      case Message.TYPE_MATCH:
      case Message.TYPE_OPEN:
      case Message.TYPE_DONE:
      case Message.TYPE_CHANGE:
        checkSeqAndPublish(root, type, accessor.getSequence(root));
        break;

      case Message.TYPE_ERROR:
        throw new WsException("received error message -> " + root.get("message"));

      default:
        throw new WsException("json root has invalid type tag -> " + type);
    }
  }

}
