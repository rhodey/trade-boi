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

  public enum Side { ASK, BID }

  protected final Long serial;
  protected String orderId;
  protected Side   side;
  protected double price;
  protected double size;
  protected double sizeRemaining;
  protected double valueRemoved;

  public Order(Long serial, String orderId, Side side, double price, double size) {
    this.serial = serial;
    init(orderId, side, price, size);
  }

  protected void init(String orderId, Side side, double price, double size) {
    this.orderId       = orderId;
    this.side          = side;
    this.price         = price;
    this.size          = size;
    this.sizeRemaining = size;
    this.valueRemoved  = 0;
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

  public double getSizeRemaining() {
    return sizeRemaining;
  }

  public double getValueRemoved() {
    return valueRemoved;
  }

  public void clearValueRemoved() {
    this.valueRemoved = 0;
  }

  protected void subtract(double size, double price) {
    sizeRemaining -= size;
  }

  public double takeSize(double size) {
    double taken   = Math.min(size, sizeRemaining);
    sizeRemaining -= taken;
    valueRemoved  += price * taken;
    return taken;
  }

}
