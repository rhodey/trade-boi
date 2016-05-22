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

package org.anhonesteffort.btc.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import org.anhonesteffort.btc.book.Limit;
import org.anhonesteffort.btc.book.LimitOrderBook;
import org.anhonesteffort.btc.book.LimitQueueListener;
import org.anhonesteffort.btc.util.LongCaster;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LimitListCurator implements LimitQueueListener {

  private final Map<Long, LimitView>      limitMap  = new HashMap<>();
  private final ObservableList<LimitView> limitList = FXCollections.observableArrayList();
  private final SortedList<LimitView>     sorted    = new SortedList<>(limitList, new SpreadSorter());

  private final LimitOrderBook orderBook;
  private final LongCaster caster;

  public LimitListCurator(LimitOrderBook orderBook, LongCaster caster) {
    this.orderBook = orderBook;
    this.caster    = caster;
    orderBook.getAskLimits().addListener(this);
    orderBook.getBidLimits().addListener(this);
  }

  public SortedList<LimitView> getLimitList() {
    return sorted;
  }

  public Optional<LimitView> getBestAsk() {
    Optional<Limit> limit = orderBook.getAskLimits().peek();
    if (limit.isPresent()) {
      return Optional.ofNullable(limitMap.get(limit.get().getPrice()));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public void onLimitAdded(Limit limit) {
    LimitView view = new LimitView(caster.toDouble(limit.getPrice()), caster.toDouble(limit.getVolume()));
    limitMap.put(limit.getPrice(), view);
    limitList.add(view);
  }

  @Override
  public void onLimitChanged(Limit limit) {
    limitMap.get(limit.getPrice()).volumeProperty().set(
        caster.toDouble(limit.getVolume())
    );
  }

  @Override
  public void onLimitRemoved(Limit limit) {
    limitList.remove(limitMap.remove(limit.getPrice()));
  }

  @Override
  public void onLimitsCleared() {
    limitMap.clear();
    limitList.clear();
  }

  private static class SpreadSorter implements Comparator<LimitView> {
    @Override
    public int compare(LimitView ask1, LimitView ask2) {
      if (ask1.getPrice() > ask2.getPrice()) {
        return -1;
      } else if (ask1.getPrice() == ask2.getPrice()) {
        return 0;
      } else {
        return 1;
      }
    }
  }

}
