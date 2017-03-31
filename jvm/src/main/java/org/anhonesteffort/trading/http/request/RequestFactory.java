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

package org.anhonesteffort.trading.http.request;

import org.anhonesteffort.trading.book.Orders;
import org.anhonesteffort.trading.http.request.model.PostOrderRequest;
import org.anhonesteffort.trading.book.Orders.Order;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.UUID;

public class RequestFactory {

  private final SecureRandom random = new SecureRandom();

  private String randomId() {
    String uuid = UUID.randomUUID().toString().substring(0, 24);
    String mac  = Long.toHexString(Math.abs(random.nextLong()));

    if (mac.length() > 12) {
      mac = mac.substring(0, 12);
    }

    while (mac.length() < 12) {
      mac += Integer.toHexString(random.nextInt(16));
    }

    return uuid + mac;
  }

  private PostOrderRequest toRequest(Order order) {
    return new PostOrderRequest(
        order.getOrderId(),
        order.isAsk() ? "sell" : "buy",
        String.format(Locale.US, "%.2f", order.getPrice()),
        String.format(Locale.US, "%.2f", order.getSize())
    );
  }

  public PostOrderRequest newAsk(Double price, Double size) {
    return toRequest(Orders.limitAsk(randomId(), price, size));
  }

  public PostOrderRequest newBid(Double price, Double size) {
    return toRequest(Orders.limitBid(randomId(), price, size));
  }

}
