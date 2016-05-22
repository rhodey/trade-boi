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

import javafx.beans.property.SimpleDoubleProperty;
import org.anhonesteffort.btc.book.Limit;
import org.anhonesteffort.btc.util.LongCaster;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;

@SuppressWarnings("unused")
public class LimitView implements Observer {

  private final LongCaster           caster;
  private final SimpleDoubleProperty price;
  private final SimpleDoubleProperty volume;

  public LimitView(Limit limit, LongCaster caster) {
    limit.addObserver(new WeakObserver(this, limit));
    this.caster = caster;
    this.price  = new SimpleDoubleProperty(caster.toDouble(limit.getPrice()));
    this.volume = new SimpleDoubleProperty(caster.toDouble(limit.getVolume()));
  }

  public double getPrice() {
    return price.get();
  }

  public void setPrice(double price) {
    this.price.set(price);
  }

  public SimpleDoubleProperty priceProperty() {
    return price;
  }

  public double getVolume() {
    return volume.get();
  }

  public void setVolume(double volume) {
    this.volume.set(volume);
  }

  public SimpleDoubleProperty volumeProperty() {
    return volume;
  }

  @Override
  public void update(Observable o, Object arg) {
    volume.set(caster.toDouble( ((Limit) o).getVolume() ));
  }

  private static class WeakObserver implements Observer {
    private final WeakReference<Observer> observer;
    private final Observable observed;

    public WeakObserver(Observer observer, Observable observed) {
      this.observer = new WeakReference<>(observer);
      this.observed = observed;
    }

    @Override
    public void update(Observable o, Object arg) {
      Observer realObserver = observer.get();
      if (realObserver != null) {
        realObserver.update(o, arg);
      } else {
        observed.deleteObserver(this);
      }
    }
  }

}
