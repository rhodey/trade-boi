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
import com.lmax.disruptor.RingBuffer;
import org.anhonesteffort.btc.OrderEvent;
import org.anhonesteffort.btc.http.response.OrderBookResponse;
import org.anhonesteffort.btc.http.response.OrderResponse;

public class WsOrderEventPublisher {

  private final RingBuffer<OrderEvent> ringBuffer;
  private long currentSeq;

  public WsOrderEventPublisher(RingBuffer<OrderEvent> ringBuffer) {
    this.ringBuffer = ringBuffer;
  }

  private OrderEvent takeNextEvent() {
    currentSeq = ringBuffer.next();
    return ringBuffer.get(currentSeq);
  }

  private void publishCurrentEvent() {
    ringBuffer.publish(currentSeq);
  }

  public void publishMessage(JsonNode root, String type) {
    // todo: convert to OrderEvent and publish to ring buffer
  }

  private void publishBookOrder(OrderResponse order) {
    OrderEvent event = takeNextEvent();
    event.initLimitOpen(order.getOrderId(), order.getSide(), order.getPrice(), order.getSize());
    publishCurrentEvent();
  }

  public void publishBook(OrderBookResponse book) {
    book.getAsks().forEach(this::publishBookOrder);
    book.getBids().forEach(this::publishBookOrder);
  }

}
