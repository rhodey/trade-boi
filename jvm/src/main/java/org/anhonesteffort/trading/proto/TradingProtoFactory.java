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

package org.anhonesteffort.trading.proto;

import org.anhonesteffort.trading.book.Orders;
import org.anhonesteffort.trading.state.Events;
import org.anhonesteffort.trading.state.Events.OrderEvent;
import org.anhonesteffort.trading.state.Events.Type;
import org.anhonesteffort.trading.book.Orders.Side;

import static org.anhonesteffort.trading.proto.TradingProto.BaseMessage;
import static org.anhonesteffort.trading.proto.TradingProto.Error;

public class TradingProtoFactory {

  public BaseMessage error(String message) {
    return BaseMessage.newBuilder()
        .setType(BaseMessage.Type.ERROR)
        .setError(Error.newBuilder().setMessage(message))
        .build();
  }

  private TradingProto.OrderEvent.Type typeFor(Type type) {
    if (type == Events.TYPE_OPEN()) {
      return TradingProto.OrderEvent.Type.OPEN;
    } else if (type == Events.TYPE_TAKE()) {
      return TradingProto.OrderEvent.Type.TAKE;
    } else if (type == Events.TYPE_REDUCE()) {
      return TradingProto.OrderEvent.Type.REDUCE;
    } else if (type == Events.TYPE_SYNC_START()) {
      return TradingProto.OrderEvent.Type.SYNC_START;
    } else {
      return TradingProto.OrderEvent.Type.SYNC_END;
    }
  }

  private TradingProto.OrderEvent.Side sideFor(Side side) {
    if (side == Orders.SIDE_ASK()) {
      return TradingProto.OrderEvent.Side.ASK;
    } else {
      return TradingProto.OrderEvent.Side.BID;
    }
  }

  private TradingProto.OrderEvent.Builder orderEventBuilder(OrderEvent event) {
    return TradingProto.OrderEvent.newBuilder()
        .setType(typeFor(event.typee()))
        .setTimeMs(event.timeMs())
        .setTimeNs(event.timeNs())
        .setOrderId(event.orderId())
        .setSide(sideFor(event.side()))
        .setPrice(event.price())
        .setSize(event.size());
  }

  public BaseMessage orderEvent(OrderEvent event) {
    return BaseMessage.newBuilder()
        .setType(BaseMessage.Type.ORDER_EVENT)
        .setOrderEvent(orderEventBuilder(event))
        .build();
  }

}
