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

package org.anhonesteffort.btc.state;

import org.anhonesteffort.btc.book.Order;

public class OrderEvent {

  public enum Type {
    LIMIT_RX, MARKET_RX, LIMIT_OPEN,
    LIMIT_DONE, MARKET_DONE, MATCH,
    LIMIT_CHANGE, MARKET_CHANGE,
    REBUILD_START, REBUILD_END
  }

  private long       nsTime;
  private Type       type;
  private String     orderId;
  private Order.Side side;
  private long       price;
  private long       size;
  private long       funds;
  private String     makerId;
  private String     takerId;
  private long       oldSize;
  private long       newSize;
  private long       oldFunds;
  private long       newFunds;

  // F12
  private void init(
      long nsTime, Type type, String orderId, Order.Side side,
      long price, long size, long funds, String makerId, String takerId,
      long oldSize, long newSize, long oldFunds, long newFunds
  ) {
    this.nsTime   = nsTime;
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

  public void initLimitRx(long nsTime, String orderId, Order.Side side, long price, long size) {
    init(nsTime, Type.LIMIT_RX, orderId, side, price, size, -1l, null, null, -1l, -1l, -1l, -1l);
  }

  public void initMarketRx(long nsTime, String orderId, Order.Side side, long size, long funds) {
    init(nsTime, Type.MARKET_RX, orderId, side, -1l, size, funds, null, null, -1l, -1l, -1l, -1l);
  }

  public void initLimitOpen(long nsTime, String orderId, Order.Side side, long price, long openSize) {
    init(nsTime, Type.LIMIT_OPEN, orderId, side, price, openSize, -1l, null, null, -1l, -1l, -1l, -1l);
  }

  public void initLimitDone(long nsTime, String orderId, Order.Side side, long price, long doneSize) {
    init(nsTime, Type.LIMIT_DONE, orderId, side, price, doneSize, -1l, null, null, -1l, -1l, -1l, -1l);
  }

  public void initMarketDone(long nsTime, String orderId, Order.Side side) {
    init(nsTime, Type.MARKET_DONE, orderId, side, -1l, -1l, -1l, null, null, -1l, -1l, -1l, -1l);
  }

  public void initMatch(long nsTime, String makerId, String takerId, Order.Side side, long price, long size) {
    init(nsTime, Type.MATCH, null, side, price, size, -1l, makerId, takerId, -1l, -1l, -1l, -1l);
  }

  public void initLimitChange(long nsTime, String orderId, Order.Side side, long price, long oldSize, long newSize) {
    init(nsTime, Type.LIMIT_CHANGE, orderId, side, price, -1l , -1l, null, null, oldSize, newSize, -1l, -1l);
  }

  public void initMarketChange(long nsTime, String orderId, Order.Side side, long oldSize, long newSize, long oldFunds, long newFunds) {
    init(nsTime, Type.MARKET_CHANGE, orderId, side, -1l, -1l, -1l, null, null, oldSize, newSize, oldFunds, newFunds);
  }

  public void initRebuildStart(long nsTime) {
    init(nsTime, Type.REBUILD_START, null, null, -1l, -1l, -1l, null, null, -1l, -1l, -1l, -1l);
  }

  public void initRebuildEnd(long nsTime) {
    init(nsTime, Type.REBUILD_END, null, null, -1l, -1l, -1l, null, null, -1l, -1l, -1l, -1l);
  }

  public long getNsTime() {
    return nsTime;
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

  public long getPrice() {
    return price;
  }

  public long getSize() {
    return size;
  }

  public long getFunds() {
    return funds;
  }

  // todo: check?
  public String getMakerId() {
    return makerId;
  }

  public String getTakerId() {
    return takerId;
  }

  public long getOldSize() {
    return oldSize;
  }

  public long getNewSize() {
    return newSize;
  }

  public long getOldFunds() {
    return oldFunds;
  }

  public long getNewFunds() {
    return newFunds;
  }

}
