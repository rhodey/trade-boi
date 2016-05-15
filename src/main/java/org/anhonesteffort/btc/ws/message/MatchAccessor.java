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

package org.anhonesteffort.btc.ws.message;

public class MatchAccessor extends MarketAccessor {

  public long getTradeId(Message message) {
    return message.root.get("trade_id").longValue();
  }

  public String getMakerOrderId(Message message) {
    return message.root.get("maker_order_id").textValue();
  }

  public String getTakerOrderId(Message message) {
    return message.root.get("taker_order_id").textValue();
  }

  public double getSize(Message message) throws NumberFormatException {
    return Double.parseDouble(message.root.get("size").textValue());
  }

  public double getPrice(Message message) throws NumberFormatException {
    return Double.parseDouble(message.root.get("price").textValue());
  }

}
