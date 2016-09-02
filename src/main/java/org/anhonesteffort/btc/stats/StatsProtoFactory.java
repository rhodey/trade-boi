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

package org.anhonesteffort.btc.stats;

import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.state.OrderEvent;

import static org.anhonesteffort.btc.stats.StatsProto.BaseMessage;
import static org.anhonesteffort.btc.stats.StatsProto.Error;

public class StatsProtoFactory {

  public BaseMessage errorMsg(String message) {
    return BaseMessage.newBuilder()
        .setType(BaseMessage.Type.ERROR)
        .setError(Error.newBuilder().setMessage(message))
        .build();
  }

  public BaseMessage resetMsg() {
    return BaseMessage.newBuilder()
        .setType(BaseMessage.Type.RESET)
        .build();
  }

  private StatsProto.Event.Type typeFor(OrderEvent.Type type) {
    if (type == OrderEvent.Type.OPEN) {
      return StatsProto.Event.Type.OPEN;
    } else if (type == OrderEvent.Type.TAKE) {
      return StatsProto.Event.Type.TAKE;
    } else {
      return StatsProto.Event.Type.REDUCE;
    }
  }

  private StatsProto.Event.Side sideFor(Order.Side side) {
    if (side == Order.Side.ASK) {
      return StatsProto.Event.Side.ASK;
    } else {
      return StatsProto.Event.Side.BID;
    }
  }

  public BaseMessage eventMsg(OrderEvent event) {
    return BaseMessage.newBuilder()
        .setType(BaseMessage.Type.EVENT)
        .setEvent(StatsProto.Event.newBuilder()
                .setType(typeFor(event.getType()))
                .setOrderId(event.getOrderId())
                .setSide(sideFor(event.getSide()))
                .setPrice(event.getPrice())
                .setSize(event.getSize())
                .build()
        ).build();
  }

  public BaseMessage latencyMsg(Long mod, Long nanoseconds) {
    return BaseMessage.newBuilder()
        .setType(BaseMessage.Type.LATENCY)
        .setLatency(StatsProto.Latency.newBuilder().setMod(mod).setNanoseconds(nanoseconds))
        .build();
  }

}
