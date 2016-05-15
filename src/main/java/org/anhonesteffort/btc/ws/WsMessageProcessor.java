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
import org.anhonesteffort.btc.message.ErrorAccessor;
import org.anhonesteffort.btc.message.MarketAccessor;
import org.anhonesteffort.btc.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class WsMessageProcessor implements EventHandler<Message> {

  private static final Logger log = LoggerFactory.getLogger(WsMessageProcessor.class);

  private final ErrorAccessor  error  = new ErrorAccessor();
  private final MarketAccessor market = new MarketAccessor();

  private Optional<Long> messageSeqLast = Optional.empty();

  private void process(Message message) {
    log.info("process -> " + market.getSequence(message));
  }

  private void rebuildOrderBook() throws IOException {
    log.warn("gotta ask coinbase for the order book");
  }

  @Override
  public void onEvent(Message message, long sequence, boolean endOfBatch) throws IOException, WsException {
    switch (message.getType()) {
      case Message.TYPE_RECEIVED:
      case Message.TYPE_OPEN:
      case Message.TYPE_DONE:
      case Message.TYPE_MATCH:
      case Message.TYPE_CHANGE:
        long messageSeq = market.getSequence(message);

        if (!messageSeqLast.isPresent() || messageSeq == (messageSeqLast.get() + 1)) {
          messageSeqLast = Optional.of(messageSeq);
          process(message);
        } else if (messageSeq <= messageSeqLast.get()) {
          log.warn("received duplicate sequence " + messageSeq + ", ignoring");
        } else {
          log.warn("received out of order seq " + messageSeq + ", expected -> " + (messageSeqLast.get() + 1));
          rebuildOrderBook();
        }
        break;

      case Message.TYPE_ERROR:
        throw new WsException("received error message: " + error.getMessage(message));

      default:
        throw new WsException("unknown message type: " + message.getType());
    }
  }

}
