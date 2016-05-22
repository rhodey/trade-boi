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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.anhonesteffort.btc.book.Limit;
import org.anhonesteffort.btc.util.LongCaster;

public class CellValueMappers {

  private final LongCaster caster;
  private final Price      price;
  private final Volume     volume;

  public CellValueMappers(LongCaster caster) {
    this.caster = caster;
    price       = new Price();
    volume      = new Volume();
  }

  public Price getPrice() {
    return price;
  }

  public Volume getVolume() {
    return volume;
  }

  public class Price implements Callback<TableColumn.CellDataFeatures<Limit, String>, ObservableValue<String>> {
    @Override
    public ObservableValue<String> call(TableColumn.CellDataFeatures<Limit, String> param) {
      return new SimpleStringProperty(
          Double.toString(caster.toDouble(param.getValue().getPrice()))
      );
    }
  }

  public class Volume implements Callback<TableColumn.CellDataFeatures<Limit, String>, ObservableValue<String>> {
    @Override
    public ObservableValue<String> call(TableColumn.CellDataFeatures<Limit, String> param) {
      return new SimpleStringProperty(
          Double.toString(caster.toDouble(param.getValue().getVolume()))
      );
    }
  }

}
