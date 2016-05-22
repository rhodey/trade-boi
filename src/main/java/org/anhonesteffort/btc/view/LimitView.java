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

import javafx.beans.Observable;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.util.Callback;
import org.anhonesteffort.btc.book.Limit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LimitView implements ChangeListener<Long> {

  private static final Logger log = LoggerFactory.getLogger(LimitView.class);

  private final SimpleLongProperty price;
  private final SimpleLongProperty volume;

  public LimitView(Limit limit) {
    limit.addListener(new WeakChangeListener<>(this));
    this.price  = new SimpleLongProperty(limit.getPrice());
    this.volume = new SimpleLongProperty(limit.getVolume());
  }

  public static Callback<LimitView, Observable[]> extractor() {
    return (LimitView limit) -> new Observable[] { limit.volume };
  }

  public Long getPrice() {
    return price.get();
  }

  public void setPrice(Long price) {
    this.price.set(price);
  }

  public Long getVolume() {
    return volume.get();
  }

  public void setVolume(Long volume) {
    this.volume.set(volume);
  }

  @Override
  public void changed(ObservableValue<? extends Long> observable, Long oldVolume, Long newVolume) {
    log.info("limit " + price.get() + " has new volume " + newVolume);
    volume.set(newVolume);
  }

}
