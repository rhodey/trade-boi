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

package org.anhonesteffort.btc.event;

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
  private float      price;
  private float      size;
  private float      funds;
  private String     makerId;
  private String     takerId;
  private float      oldSize;
  private float      newSize;
  private float      oldFunds;
  private float      newFunds;

  // F12
  private void init(
      Type type, String orderId, Order.Side side,
      float price, float size, float funds, String makerId, String takerId,
      float oldSize, float newSize, float oldFunds, float newFunds
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

  public void initLimitRx(String orderId, Order.Side side, float price, float size) {
    init(Type.LIMIT_RX, orderId, side, price, size, -1f, null, null, -1f, -1f, -1f, -1f);
  }

  public void initMarketRx(String orderId, Order.Side side, float size, float funds) {
    init(Type.MARKET_RX, orderId, side, -1f, size, funds, null, null, -1f, -1f, -1f, -1f);
  }

  public void initLimitOpen(String orderId, Order.Side side, float price, float openSize) {
    init(Type.LIMIT_OPEN, orderId, side, price, openSize, -1f, null, null, -1f, -1f, -1f, -1f);
  }

  public void initLimitDone(String orderId, Order.Side side, float price, float doneSize) {
    init(Type.LIMIT_DONE, orderId, side, price, doneSize, -1f, null, null, -1f, -1f, -1f, -1f);
  }

  public void initMarketDone(String orderId, Order.Side side) {
    init(Type.MARKET_DONE, orderId, side, -1f, -1f, -1f, null, null, -1f, -1f, -1f, -1f);
  }

  public void initMatch(String makerId, String takerId, Order.Side side, float price, float size) {
    init(Type.MATCH, null, side, price, size, -1f, makerId, takerId, -1f, -1f, -1f, -1f);
  }

  public void initLimitChange(String orderId, Order.Side side, float price, float oldSize, float newSize) {
    init(Type.LIMIT_CHANGE, orderId, side, price, -1f , -1f, null, null, oldSize, newSize, -1f, -1f);
  }

  public void initMarketChange(String orderId, Order.Side side, float oldSize, float newSize, float oldFunds, float newFunds) {
    init(Type.MARKET_CHANGE, orderId, side, -1f, -1f, -1f, null, null, oldSize, newSize, oldFunds, newFunds);
  }

  public void initRebuildStart() {
    init(Type.REBUILD_START, null, null, -1f, -1f, -1f, null, null, -1f, -1f, -1f, -1f);
  }

  public void initRebuildEnd() {
    init(Type.REBUILD_END, null, null, -1f, -1f, -1f, null, null, -1f, -1f, -1f, -1f);
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

  public float getPrice() {
    return price;
  }

  public float getSize() {
    return size;
  }

  public float getFunds() {
    return funds;
  }

  public String getMakerId() {
    return makerId;
  }

  public String getTakerId() {
    return takerId;
  }

  public float getOldSize() {
    return oldSize;
  }

  public float getNewSize() {
    return newSize;
  }

  public float getOldFunds() {
    return oldFunds;
  }

  public float getNewFunds() {
    return newFunds;
  }

}
