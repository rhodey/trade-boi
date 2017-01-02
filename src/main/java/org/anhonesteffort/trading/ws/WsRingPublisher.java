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
import com.lmax.disruptor.RingBuffer;
import org.anhonesteffort.trading.state.GdaxEvent;
import org.anhonesteffort.trading.http.response.model.GetOrderBookResponse;
import org.anhonesteffort.trading.http.response.model.GetOrderBookResponseEntry;
import org.anhonesteffort.trading.ws.message.ChangeAccessor;
import org.anhonesteffort.trading.ws.message.DoneAccessor;
import org.anhonesteffort.trading.ws.message.Accessor;
import org.anhonesteffort.trading.ws.message.MatchAccessor;
import org.anhonesteffort.trading.ws.message.OpenAccessor;
import org.anhonesteffort.trading.ws.message.ReceivedAccessor;
import org.anhonesteffort.trading.book.Order;
import org.anhonesteffort.trading.util.LongCaster;

public class WsRingPublisher {

  private final Accessor         base    = new Accessor();
  private final ReceivedAccessor receive = new ReceivedAccessor();
  private final MatchAccessor    match   = new MatchAccessor();
  private final OpenAccessor     open    = new OpenAccessor();
  private final DoneAccessor     done    = new DoneAccessor();
  private final ChangeAccessor   change  = new ChangeAccessor();

  private final RingBuffer<GdaxEvent> ringBuffer;
  private final LongCaster caster;
  private long currentSeq;

  public WsRingPublisher(RingBuffer<GdaxEvent> ringBuffer, LongCaster caster) {
    this.ringBuffer = ringBuffer;
    this.caster     = caster;
  }

  private GdaxEvent takeNextEvent() {
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

  public void publishMessage(JsonNode root, String type, long nanoseconds) throws WsException {
    Order.Side side  = getSideOrThrow(root);
    GdaxEvent  event = takeNextEvent();

    switch (type) {
      case Accessor.TYPE_RECEIVED:
        if (receive.getOrderType(root).equals("limit")) {
          event.initLimitRx(
              nanoseconds, receive.getOrderId(root), receive.getClientOid(root), side,
              caster.fromDouble(receive.getPrice(root)), caster.fromDouble(receive.getSize(root))
          );
        } else if (receive.getOrderType(root).equals("market")) {
          event.initMarketRx(
              nanoseconds, receive.getOrderId(root), side,
              caster.fromDouble(receive.getSize(root)), caster.fromDouble(receive.getFunds(root))
          );
        } else {
          throw new WsException("received message has invalid order_type");
        }
        break;

      case Accessor.TYPE_MATCH:
        event.initMatch(
            nanoseconds, match.getMakerOrderId(root), match.getTakerOrderId(root), side,
            caster.fromDouble(match.getPrice(root)), caster.fromDouble(match.getSize(root))
        );
        break;

      case Accessor.TYPE_OPEN:
        event.initLimitOpen(
            nanoseconds, open.getOrderId(root), side,
            caster.fromDouble(open.getPrice(root)), caster.fromDouble(open.getRemainingSize(root))
        );
        break;

      case Accessor.TYPE_DONE:
        if (done.getOrderType(root).equals("limit")) {
          event.initLimitDone(
              nanoseconds, done.getOrderId(root), side,
              caster.fromDouble(done.getPrice(root)), caster.fromDouble(done.getRemainingSize(root))
          );
        } else if (done.getOrderType(root).equals("market")) {
          event.initMarketDone(nanoseconds, done.getOrderId(root), side);
        } else {
          throw new WsException("done message has invalid order_type");
        }
        break;

      case Accessor.TYPE_CHANGE:
        if (change.getPrice(root) > 0d) {
          event.initLimitChange(
              nanoseconds, change.getOrderId(root), side, caster.fromDouble(change.getPrice(root)),
              caster.fromDouble(change.getOldSize(root)), caster.fromDouble(change.getNewSize(root))
          );
        } else {
          event.initMarketChange(
              nanoseconds, change.getOrderId(root), side,
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

  private void publishBookOrder(GetOrderBookResponseEntry order, long nanoseconds) {
    GdaxEvent event = takeNextEvent();
    event.initLimitOpen(
        nanoseconds, order.getOrderId(), order.getSide(),
        caster.fromDouble(order.getPrice()), caster.fromDouble(order.getSize())
    );
    publishCurrentEvent();
  }

  public void publishBook(GetOrderBookResponse book, long nanoseconds) {
    GdaxEvent event = takeNextEvent();
    event.initRebuildStart(nanoseconds);
    publishCurrentEvent();

    book.getAsks().forEach(ask -> this.publishBookOrder(ask, nanoseconds));
    book.getBids().forEach(bid -> this.publishBookOrder(bid, nanoseconds));

    event = takeNextEvent();
    event.initRebuildEnd(nanoseconds);
    publishCurrentEvent();
  }

}
