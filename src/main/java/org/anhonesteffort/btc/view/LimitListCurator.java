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
import org.anhonesteffort.btc.util.LongCaster;

import java.util.Comparator;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

public class LimitListCurator {

  private final ObservableList<LimitView> askLimits = FXCollections.observableArrayList();
  private final ObservableList<LimitView> bidLimits = FXCollections.observableArrayList();

  private final LongCaster caster;

  public LimitListCurator(LimitOrderBook orderBook, LongCaster caster) {
    this.caster = caster;
    orderBook.getAskLimits().addObserver(new AskLimitCallback());
    orderBook.getBidLimits().addObserver(new BidLimitCallback());
  }

  public SortedList<LimitView> getAskLimits() {
    return new SortedList<>(askLimits, new AskSorter());
  }

  public SortedList<LimitView> getBidLimits() {
    return new SortedList<>(bidLimits, new BidSorter());
  }

  private class AskLimitCallback implements Observer {
    @Override
    public void update(Observable o, Object arg) {
      if (arg == null) {
        askLimits.clear();
      } else {
        Limit               addOrRemove  = (Limit) arg;
        Optional<LimitView> existingView = askLimits.stream().filter(
            view -> view.getLimit().equals(addOrRemove)
        ).findAny();

        if (existingView.isPresent()) {
          askLimits.remove(existingView.get());
        } else {
          askLimits.add(new LimitView(addOrRemove, caster));
        }
      }
    }
  }

  private class BidLimitCallback implements Observer {
    @Override
    public void update(Observable o, Object arg) {
      if (arg == null) {
        bidLimits.clear();
      } else {
        Limit               addOrRemove  = (Limit) arg;
        Optional<LimitView> existingView = bidLimits.stream().filter(
            view -> view.getLimit().equals(addOrRemove)
        ).findAny();

        if (existingView.isPresent()) {
          bidLimits.remove(existingView.get());
        } else {
          bidLimits.add(new LimitView(addOrRemove, caster));
        }
      }
    }
  }

  private static class AskSorter implements Comparator<LimitView> {
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

  private static class BidSorter implements Comparator<LimitView> {
    @Override
    public int compare(LimitView bid1, LimitView bid2) {
      if (bid1.getPrice() < bid2.getPrice()) {
        return -1;
      } else if (bid1.getPrice() == bid2.getPrice()) {
        return 0;
      } else {
        return 1;
      }
    }
  }

}
