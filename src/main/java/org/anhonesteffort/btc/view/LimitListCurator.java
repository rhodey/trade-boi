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
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.anhonesteffort.btc.book.LimitOrderBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;
import java.util.stream.Collectors;

public class LimitListCurator extends TimerTask {

  private static final Logger log = LoggerFactory.getLogger(LimitListCurator.class);

  private final ObservableList<LimitView> limits = FXCollections.observableArrayList(LimitView.extractor());
  private final LimitOrderBook orderBook;

  public LimitListCurator(LimitOrderBook orderBook) {
    this.orderBook = orderBook;
    limits.addListener(new ListChangeListener<LimitView>() {
      @Override
      public void onChanged(Change<? extends LimitView> c) {
        log.info("!!! why not !!!");
      }
    });
  }

  public ObservableList<LimitView> getLimits() {
    return limits;
  }

  @Override
  public void run() {
    limits.clear();
    limits.addAll(orderBook.getAskLimits().stream().map(
        LimitView::new
    ).limit(10).collect(Collectors.toList()));
  }

}
