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

import org.anhonesteffort.btc.book.HeuristicLimitOrderBook;
import org.anhonesteffort.btc.book.OrderPool;

public class MatchingOrderBookBuilder extends MarketOrderBookBuilder {

  public MatchingOrderBookBuilder(HeuristicLimitOrderBook book, OrderPool pool) {
    super(book, pool);
  }

  @Override
  protected void onEvent(OrderEvent event) throws OrderEventException {
    super.onEvent(event);
    /*
    todo:
      1. maintain a collection of received limit and market orders
      2. adjust orders when size or funds change
      3. remove orders when open or canceled/filled/done
      4. when MATCH event comes in:
        1. pull order taker_order_id from collection
        2. add taker to the order book
        3. make sure take result agrees with MATCH event

      ^^ this can't work because taker order may be IOC or FOK ^^
      ^^ and our order book can only simulate GTC              ^^
    */
  }

}
