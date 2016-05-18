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

public class OrderEvent {

  public enum Type {
    LIMIT_RX, MARKET_RX, LIMIT_OPEN,
    LIMIT_DONE, MARKET_DONE, MATCH,
    LIMIT_CHANGE, MARKET_CHANGE,
    REBUILD_START, REBUILD_END
  }

  private Type       type;
  private String     orderId;
  private Order.Side side;
  private double     price;
  private double     size;
  private double     funds;
  private String     makerId;
  private String     takerId;
  private double     oldSize;
  private double     newSize;
  private double     oldFunds;
  private double     newFunds;

  // F12
  private void init(
      Type type, String orderId, Order.Side side,
      double price, double size, double funds, String makerId, String takerId,
      double oldSize, double newSize, double oldFunds, double newFunds
  ) {
    this.type     = type;
    this.orderId  = orderId;
    this.side     = side;
    this.price    = price;
    this.size     = size;
    this.funds    = funds;
    this.makerId  = makerId;
    this.takerId  = takerId;
    this.oldSize  = oldSize;
    this.newSize  = newSize;
    this.oldFunds = oldFunds;
    this.newFunds = newFunds;
  }

  public void initLimitRx(String orderId, Order.Side side, double price, double size) {
    init(Type.LIMIT_RX, orderId, side, price, size, 0, null, null, 0, 0, 0, 0);
  }

  public void initMarketRx(String orderId, Order.Side side, double size, double funds) {
    init(Type.MARKET_RX, orderId, side, 0, size, funds, null, null, 0, 0, 0, 0);
  }

  public void initLimitOpen(String orderId, Order.Side side, double price, double openSize) {
    init(Type.LIMIT_OPEN, orderId, side, price, openSize, 0, null, null, 0, 0, 0, 0);
  }

  public void initLimitDone(String orderId, Order.Side side, double price, double doneSize) {
    init(Type.LIMIT_DONE, orderId, side, price, doneSize, 0, null, null, 0, 0, 0, 0);
  }

  public void initMarketDone(String orderId, Order.Side side) {
    init(Type.MARKET_DONE, orderId, side, 0, 0, 0, null, null, 0, 0, 0, 0);
  }

  public void initMatch(String makerId, String takerId, Order.Side side, double price, double size) {
    init(Type.MATCH, null, side, price, size, 0, makerId, takerId, 0, 0, 0, 0);
  }

  public void initLimitChange(String orderId, Order.Side side, double price, double oldSize, double newSize) {
    init(Type.LIMIT_CHANGE, orderId, side, price, 0, 0, null, null, oldSize, newSize, 0, 0);
  }

  public void initMarketChange(String orderId, Order.Side side, double oldSize, double newSize, double oldFunds, double newFunds) {
    init(Type.MARKET_CHANGE, orderId, side, 0, 0, 0, null, null, oldSize, newSize, oldFunds, newFunds);
  }

  public void initRebuildStart() {
    init(Type.REBUILD_START, null, null, 0, 0, 0, null, null, 0, 0, 0, 0);
  }

  public void initRebuildEnd() {
    init(Type.REBUILD_END, null, null, 0, 0, 0, null, null, 0, 0, 0, 0);
  }

  public Type getType() {
    return type;
  }

  public String getOrderId() {
    return orderId;
  }

  public Order.Side getSide() {
    return side;
  }

  public double getPrice() {
    return price;
  }

  public double getSize() {
    return size;
  }

  public double getFunds() {
    return funds;
  }

  public String getMakerId() {
    return makerId;
  }

  public String getTakerId() {
    return takerId;
  }

  public double getOldSize() {
    return oldSize;
  }

  public double getNewSize() {
    return newSize;
  }

  public double getOldFunds() {
    return oldFunds;
  }

  public double getNewFunds() {
    return newFunds;
  }

}
