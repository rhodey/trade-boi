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
import org.anhonesteffort.btc.book.TakeResult;

import java.util.stream.Collectors;

import static org.anhonesteffort.btc.stats.StatsProto.BaseMessage;
import static org.anhonesteffort.btc.stats.StatsProto.Error;
import static org.anhonesteffort.btc.stats.StatsProto.TakeEvent;

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

  private StatsProto.Order order(Order order) {
    return StatsProto.Order.newBuilder()
        .setOrderId(order.getOrderId())
        .setSide((order.getSide() == Order.Side.ASK) ? StatsProto.Order.Side.ASK : StatsProto.Order.Side.BID)
        .setPrice(order.getPrice())
        .setSize(order.getSize())
        .setSizeRemaining(order.getSizeRemaining())
        .setValueRemoved(order.getValueRemoved())
        .build();
  }

  private TakeEvent takeEvent(TakeResult takeResult) {
    return TakeEvent.newBuilder()
        .setTaker(order(takeResult.getTaker()))
        .addAllMakers(takeResult.getMakers().stream().map(this::order).collect(Collectors.toList()))
        .setTakeSize(takeResult.getTakeSize())
        .setTakeValue(takeResult.getTakeValue())
        .build();
  }

  public BaseMessage takeEventMsg(TakeResult takeResult) {
    return BaseMessage.newBuilder()
        .setType(BaseMessage.Type.TAKE)
        .setTakeEvent(takeEvent(takeResult))
        .build();
  }

}
