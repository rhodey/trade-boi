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

package org.anhonesteffort.btc.book;

public class Order {

  public enum Side { BUY, SELL }

  private String orderId;
  private Side   side;
  private double price;
  private double size;
  private double remaining;

  public Order(String orderId, Side side, double price, double size) {
    init(orderId, side, price, size);
  }

  public void init(String orderId, Side side, double price, double size) {
    this.orderId   = orderId;
    this.side      = side;
    this.price     = price;
    this.size      = size;
    this.remaining = size;
  }

  public String getOrderId() {
    return orderId;
  }

  public Side getSide() {
    return side;
  }

  public double getPrice() {
    return price;
  }

  public double getSize() {
    return size;
  }

  public double getRemaining() {
    return remaining;
  }

  public double takeSize(double size) {
    double taken = Math.min(size, remaining);
    remaining    = remaining - taken;
    return taken;
  }

}
