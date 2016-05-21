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
import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.http.HttpException;

public class OrderResponse {

  private final Order.Side side;
  private final String orderId;
  private final float price;
  private final float size;

  public OrderResponse(Order.Side side, JsonNode node) throws HttpException {
    this.side = side;
    orderId   = node.get(2).textValue();
    try {

      price = Float.parseFloat(node.get(0).textValue());
      size  = Float.parseFloat(node.get(1).textValue());

    } catch (NumberFormatException e) {
      throw new HttpException("order price or size is invalid", e);
    }
  }

  public Order.Side getSide() {
    return side;
  }

  public String getOrderId() {
    return orderId;
  }

  public float getPrice() {
    return price;
  }

  public float getSize() {
    return size;
  }

}
