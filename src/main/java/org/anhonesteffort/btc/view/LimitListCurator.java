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

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.anhonesteffort.btc.book.LimitOrderBook;
import org.anhonesteffort.btc.util.LongCaster;

import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;

public class LimitListCurator implements Observer {

  private final ObservableList<LimitView> limits = FXCollections.observableArrayList();
  private final LimitOrderBook orderBook;
  private final LongCaster caster;

  public LimitListCurator(LimitOrderBook orderBook, LongCaster caster) {
    this.orderBook = orderBook;
    this.caster    = caster;
    orderBook.getAskLimits().addObserver(this);
    orderBook.getBidLimits().addObserver(this);
  }

  public ObservableList<LimitView> getLimits() {
    return limits;
  }

  @Override
  public void update(Observable o, Object arg) {
    Platform.runLater(() -> {
      limits.clear();
      limits.addAll(orderBook.getAskLimits().stream().map(
          limit -> new LimitView(limit, caster)
      ).limit(10).collect(Collectors.toList()));
    });
  }

}
