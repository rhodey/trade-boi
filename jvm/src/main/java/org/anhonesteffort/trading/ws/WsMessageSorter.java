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

package org.anhonesteffort.trading.ws;

import com.fasterxml.jackson.databind.JsonNode;
import org.anhonesteffort.trading.http.HttpClientWrapper;
import org.anhonesteffort.trading.http.response.model.GetOrderBookResponse;
import org.anhonesteffort.trading.ws.message.Accessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class WsMessageSorter {

  private static final Logger log = LoggerFactory.getLogger(WsMessageSorter.class);

  private final Accessor          accessor = new Accessor();
  private final WsRingPublisher   publisher;
  private final HttpClientWrapper http;

  private Optional<Long> messageSeqLast = Optional.empty();

  public WsMessageSorter(WsRingPublisher publisher, HttpClientWrapper http) {
    this.publisher = publisher;
    this.http      = http;
  }

  private void checkSeqAndPublish(JsonNode root, String type, long sequence, long nanoseconds)
      throws WsException, InterruptedException, ExecutionException
  {
    if (!messageSeqLast.isPresent()) {
      GetOrderBookResponse orderBook = http.geOrderBook().get();
      messageSeqLast = Optional.of(orderBook.getSequence());
      publisher.publishBook(orderBook, System.nanoTime());
    } else if (sequence == (messageSeqLast.get() + 1l)) {
      messageSeqLast = Optional.of(sequence);
      publisher.publishMessage(root, type, nanoseconds);
    } else if (sequence > messageSeqLast.get()) {
      log.warn("received out of order seq -> " + sequence + ", expected -> " + (messageSeqLast.get() + 1));
      GetOrderBookResponse orderBook = http.geOrderBook().get();
      messageSeqLast = Optional.of(orderBook.getSequence());
      publisher.publishBook(orderBook, System.nanoTime());
    }
  }

  public void sort(JsonNode root, long nanoseconds)
      throws WsException, InterruptedException, ExecutionException
  {
    String type = accessor.getType(root);
    switch (type) {
      case Accessor.TYPE_RECEIVED:
      case Accessor.TYPE_MATCH:
      case Accessor.TYPE_OPEN:
      case Accessor.TYPE_DONE:
      case Accessor.TYPE_CHANGE:
        checkSeqAndPublish(root, type, accessor.getSequence(root), nanoseconds);
        break;

      case Accessor.TYPE_ERROR:
        throw new WsException("received error message -> " + root.get("message"));

      default:
        throw new WsException("json root has invalid type tag -> " + type);
    }
  }

}
