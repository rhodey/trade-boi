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

package org.anhonesteffort.btc.http.response;

import com.fasterxml.jackson.databind.JsonNode;

public class OrderResponse {

  public enum Side { BUY, SELL }

  private final Side   side;
  private final String orderId;
  private final double price;
  private final double size;

  public OrderResponse(Side side, JsonNode node) throws NumberFormatException {
    this.side = side;
    orderId   = node.get(2).textValue();
    price     = Double.parseDouble(node.get(0).textValue());
    size      = Double.parseDouble(node.get(1).textValue());
  }

  public Side getSide() {
    return side;
  }

  public String getOrderId() {
    return orderId;
  }

  public double getPrice() {
    return price;
  }

  public double getSize() {
    return size;
  }

  @Override
  public String toString() {
    return "side: " + getSide() + ", price: " + getPrice() + ", size: " + getSize() + ", id: " + getOrderId();
  }

}
