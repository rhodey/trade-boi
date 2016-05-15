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

package org.anhonesteffort.btc.message;

public class ChangeAccessor extends MarketAccessor {

  public String getOrderId(Message message) {
    return message.root.get("order_id").textValue();
  }

  public double getNewSize(Message message) {
    return doubleValueOrNeg(message.root, "new_size");
  }

  public double getOldSize(Message message) {
    return doubleValueOrNeg(message.root, "old_size");
  }

  public double getNewFunds(Message message) {
    return doubleValueOrNeg(message.root, "new_funds");
  }

  public double getOldFunds(Message message) {
    return doubleValueOrNeg(message.root, "old_funds");
  }

  public double getPrice(Message message) {
    return doubleValueOrNeg(message.root, "price");
  }

}
