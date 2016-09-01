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

package org.anhonesteffort.btc.http.request;

import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.http.request.model.PostOrderRequest;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.UUID;

public class RequestFactory {

  private final SecureRandom random = new SecureRandom();

  public PostOrderRequest newOrder(Order.Side side, Double price, Double size) {
    String uuid = UUID.randomUUID().toString().substring(0, 24);
    String mac  = Long.toHexString(Math.abs(random.nextLong()));

    if (mac.length() > 12) {
      mac = mac.substring(0, 12);
    }

    while (mac.length() < 12) {
      mac += Integer.toHexString(random.nextInt(16));
    }

    return new PostOrderRequest(
        uuid + mac,
        (side == Order.Side.BID) ? "buy" : "sell",
        String.format(Locale.US, "%.2f", price),
        String.format(Locale.US, "%.2f", size)
    );
  }

}
