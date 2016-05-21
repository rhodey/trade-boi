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
import org.anhonesteffort.btc.event.OrderEvent;
import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.http.response.OrderBookResponse;
import org.anhonesteffort.btc.http.response.OrderResponse;
import org.anhonesteffort.btc.util.LongCaster;
import org.anhonesteffort.btc.ws.message.ChangeAccessor;
import org.anhonesteffort.btc.ws.message.DoneAccessor;
import org.anhonesteffort.btc.ws.message.Accessor;
import org.anhonesteffort.btc.ws.message.MatchAccessor;
import org.anhonesteffort.btc.ws.message.OpenAccessor;
import org.anhonesteffort.btc.ws.message.ReceivedAccessor;

public class WsOrderEventPublisher {

  private final Accessor         base    = new Accessor();
  private final ReceivedAccessor receive = new ReceivedAccessor();
  private final MatchAccessor    match   = new MatchAccessor();
  private final OpenAccessor     open    = new OpenAccessor();
  private final DoneAccessor     done    = new DoneAccessor();
  private final ChangeAccessor   change  = new ChangeAccessor();

  private final RingBuffer<OrderEvent> ringBuffer;
  private final LongCaster caster;
  private long currentSeq;

  public WsOrderEventPublisher(RingBuffer<OrderEvent> ringBuffer, LongCaster caster) {
    this.ringBuffer = ringBuffer;
    this.caster     = caster;
  }

  private OrderEvent takeNextEvent() {
    currentSeq = ringBuffer.next();
    return ringBuffer.get(currentSeq);
  }

  private void publishCurrentEvent() {
    ringBuffer.publish(currentSeq);
  }

  private Order.Side getSideOrThrow(JsonNode root) throws WsException {
    String side = base.getSide(root);
    switch (side) {
      case "sell":
        return Order.Side.ASK;
      case "buy":
        return Order.Side.BID;
      default:
        throw new WsException("message has invalid side -> " + side);
    }
  }

  public void publishMessage(JsonNode root, String type, long nsTime) throws WsException {
    Order.Side side  = getSideOrThrow(root);
    OrderEvent event = takeNextEvent();

    switch (type) {
      case Accessor.TYPE_RECEIVED:
        if (receive.getOrderType(root).equals("limit")) {
          event.initLimitRx(
              nsTime, receive.getOrderId(root), side,
              caster.fromDouble(receive.getPrice(root)), caster.fromDouble(receive.getSize(root))
          );
        } else if (receive.getOrderType(root).equals("market")) {
          event.initMarketRx(
              nsTime, receive.getOrderId(root), side,
              caster.fromDouble(receive.getSize(root)), caster.fromDouble(receive.getFunds(root))
          );
        } else {
          throw new WsException("received message has invalid order_type");
        }
        break;

      case Accessor.TYPE_MATCH:
        event.initMatch(
            nsTime, match.getMakerOrderId(root), match.getTakerOrderId(root), side,
            caster.fromDouble(match.getPrice(root)), caster.fromDouble(match.getSize(root))
        );
        break;

      case Accessor.TYPE_OPEN:
        event.initLimitOpen(
            nsTime, open.getOrderId(root), side,
            caster.fromDouble(open.getPrice(root)), caster.fromDouble(open.getRemainingSize(root))
        );
        break;

      case Accessor.TYPE_DONE:
        if (done.getOrderType(root).equals("limit")) {
          event.initLimitDone(
              nsTime, done.getOrderId(root), side,
              caster.fromDouble(done.getPrice(root)), caster.fromDouble(done.getRemainingSize(root))
          );
        } else if (done.getOrderType(root).equals("market")) {
          event.initMarketDone(nsTime, done.getOrderId(root), side);
        } else {
          throw new WsException("done message has invalid order_type");
        }
        break;

      case Accessor.TYPE_CHANGE:
        if (change.getPrice(root) > 0f) {
          event.initLimitChange(
              nsTime, change.getOrderId(root), side, caster.fromDouble(change.getPrice(root)),
              caster.fromDouble(change.getOldSize(root)), caster.fromDouble(change.getNewSize(root))
          );
        } else {
          event.initMarketChange(
              nsTime, change.getOrderId(root), side,
              caster.fromDouble(change.getOldSize(root)), caster.fromDouble(change.getNewSize(root)),
              caster.fromDouble(change.getOldFunds(root)), caster.fromDouble(change.getNewFunds(root))
          );
        }
        break;

      default:
        throw new WsException("unknown message type -> " + type);
    }

    publishCurrentEvent();
  }

  private void publishBookOrder(OrderResponse order, long nsTime) {
    OrderEvent event = takeNextEvent();
    event.initLimitOpen(
        nsTime, order.getOrderId(), order.getSide(),
        caster.fromDouble(order.getPrice()), caster.fromDouble(order.getSize())
    );
    publishCurrentEvent();
  }

  public void publishBook(OrderBookResponse book, long nsTime) {
    OrderEvent event = takeNextEvent();
    event.initRebuildStart(nsTime);
    publishCurrentEvent();

    book.getAsks().forEach(ask -> this.publishBookOrder(ask, nsTime));
    book.getBids().forEach(bid -> this.publishBookOrder(bid, nsTime));

    event = takeNextEvent();
    event.initRebuildEnd(nsTime);
    publishCurrentEvent();
  }

}
