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

package org.anhonesteffort.btc;

import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.ws.message.ChangeAccessor;
import org.anhonesteffort.btc.ws.message.DoneAccessor;
import org.anhonesteffort.btc.ws.message.MarketAccessor;
import org.anhonesteffort.btc.ws.message.MatchAccessor;
import org.anhonesteffort.btc.ws.message.Message;
import org.anhonesteffort.btc.ws.message.OpenAccessor;
import org.anhonesteffort.btc.ws.message.ReceivedAccessor;

import java.io.IOException;

public class OrderEventFactory {

  private final MarketAccessor   base    = new MarketAccessor();
  private final ReceivedAccessor receive = new ReceivedAccessor();
  private final MatchAccessor    match   = new MatchAccessor();
  private final OpenAccessor     open    = new OpenAccessor();
  private final DoneAccessor     done    = new DoneAccessor();
  private final ChangeAccessor   change  = new ChangeAccessor();

  public OrderEvent eventFor(OrderEvent target, Message source) throws IOException {
    Order.Side side = null;
    if (base.getSide(source).equals("sell")) {
      side = Order.Side.ASK;
    } else if (base.getSide(source).equals("buy")) {
      side = Order.Side.BID;
    } else {
      throw new IOException("message has invalid side");
    }

    switch (source.getType()) {
      case Message.TYPE_RECEIVED:
        if (receive.getOrderType(source).equals("limit")) {
          target.initLimitRx(receive.getOrderId(source), side, receive.getPrice(source), receive.getSize(source));
        } else if (receive.getOrderType(source).equals("market")) {
          target.initMarketRx(receive.getOrderId(source), side, receive.getSize(source), receive.getFunds(source));
        } else {
          throw new IOException("rx message has invalid order_type");
        }
        break;

      case Message.TYPE_MATCH:
        target.initMatch(match.getMakerOrderId(source), match.getTakerOrderId(source), side, match.getPrice(source), match.getSize(source));
        break;

      case Message.TYPE_OPEN:
        target.initLimitOpen(open.getOrderId(source), side, open.getPrice(source), open.getRemainingSize(source));
        break;

      case Message.TYPE_DONE:
        if (done.getOrderType(source).equals("limit")) {
          target.initLimitDone(done.getOrderId(source), side, done.getPrice(source), done.getRemainingSize(source));
        } else if (done.getOrderType(source).equals("market")) {
          target.initMarketDone(done.getOrderId(source), side);
        } else {
          throw new IOException("done message has invalid order_type");
        }
        break;

      case Message.TYPE_CHANGE:
        if (change.getPrice(source) > 0) {
          target.initLimitChange(change.getOrderId(source), side, change.getPrice(source), change.getOldSize(source), change.getNewSize(source));
        } else {
          target.initMarkteChange(change.getOrderId(source), side, change.getOldSize(source), change.getNewSize(source), change.getOldFunds(source), change.getNewFunds(source));
        }

      default:
        throw new IOException("invalid message type -> " + source.getType());
    }

    return target;
  }

}
