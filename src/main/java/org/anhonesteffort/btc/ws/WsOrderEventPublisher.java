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
import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.http.response.OrderBookResponse;
import org.anhonesteffort.btc.http.response.OrderResponse;
import org.anhonesteffort.btc.ws.message.ChangeAccessor;
import org.anhonesteffort.btc.ws.message.DoneAccessor;
import org.anhonesteffort.btc.ws.message.MarketAccessor;
import org.anhonesteffort.btc.ws.message.MatchAccessor;
import org.anhonesteffort.btc.ws.message.Message;
import org.anhonesteffort.btc.ws.message.OpenAccessor;
import org.anhonesteffort.btc.ws.message.ReceivedAccessor;

public class WsOrderEventPublisher {

  private final MarketAccessor   base    = new MarketAccessor();
  private final ReceivedAccessor receive = new ReceivedAccessor();
  private final MatchAccessor    match   = new MatchAccessor();
  private final OpenAccessor     open    = new OpenAccessor();
  private final DoneAccessor     done    = new DoneAccessor();
  private final ChangeAccessor   change  = new ChangeAccessor();

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

  private Order.Side getSideOrThrow(JsonNode root) throws WsException {
    if (base.getSide(root).equals("sell")) {
      return Order.Side.ASK;
    } else if (base.getSide(root).equals("buy")) {
      return Order.Side.BID;
    } else {
      throw new WsException("message has invalid side");
    }
  }

  public void publishMessage(JsonNode root, String type) throws WsException {
    Order.Side side  = getSideOrThrow(root);
    OrderEvent event = takeNextEvent();

    switch (type) {
      case Message.TYPE_RECEIVED:
        if (receive.getOrderType(root).equals("limit")) {
          event.initLimitRx(receive.getOrderId(root), side, receive.getPrice(root), receive.getSize(root));
        } else if (receive.getOrderType(root).equals("market")) {
          event.initMarketRx(receive.getOrderId(root), side, receive.getSize(root), receive.getFunds(root));
        } else {
          throw new WsException("rx message has invalid order_type");
        }
        break;

      case Message.TYPE_MATCH:
        event.initMatch(match.getMakerOrderId(root), match.getTakerOrderId(root), side, match.getPrice(root), match.getSize(root));
        break;

      case Message.TYPE_OPEN:
        event.initLimitOpen(open.getOrderId(root), side, open.getPrice(root), open.getRemainingSize(root));
        break;

      case Message.TYPE_DONE:
        if (done.getOrderType(root).equals("limit")) {
          event.initLimitDone(done.getOrderId(root), side, done.getPrice(root), done.getRemainingSize(root));
        } else if (done.getOrderType(root).equals("market")) {
          event.initMarketDone(done.getOrderId(root), side);
        } else {
          throw new WsException("done message has invalid order_type");
        }
        break;

      case Message.TYPE_CHANGE:
        if (change.getPrice(root) > 0) {
          event.initLimitChange(change.getOrderId(root), side, change.getPrice(root), change.getOldSize(root), change.getNewSize(root));
        } else {
          event.initMarkteChange(change.getOrderId(root), side, change.getOldSize(root), change.getNewSize(root), change.getOldFunds(root), change.getNewFunds(root));
        }
        break;

      default:
        throw new WsException("unknown message type -> " + type);
    }

    publishCurrentEvent();
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
