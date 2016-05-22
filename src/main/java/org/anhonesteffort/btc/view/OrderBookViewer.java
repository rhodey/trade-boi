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
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.anhonesteffort.btc.book.LimitOrderBook;
import org.anhonesteffort.btc.util.LongCaster;

import java.util.Optional;

public class OrderBookViewer {

  private final TableView<LimitView> table = new TableView<>();
  private final LimitListCurator curator;

  public OrderBookViewer(LimitOrderBook orderBook, LongCaster caster) {
    curator = new LimitListCurator(orderBook, caster);
  }

  @SuppressWarnings("unchecked")
  public void start(Stage stage) {
    stage.setTitle("Coinbase Trading");
    stage.setWidth(300);
    stage.setHeight(600);

    TableColumn priceCol  = new TableColumn("price");
    TableColumn volumeCol = new TableColumn("volume");

    priceCol.setMinWidth(150);
    volumeCol.setMinWidth(150);
    priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
    volumeCol.setCellValueFactory(new PropertyValueFactory<>("volume"));

    table.setItems(curator.getLimitList());
    table.getColumns().addAll(volumeCol, priceCol);
    table.setPrefHeight(600);

    Scene scene = new Scene(new Group());
    VBox  vbox  = new VBox();

    vbox.setSpacing(5);
    vbox.setPadding(new Insets(0, 0, 0, 0));
    vbox.getChildren().addAll(table);

    ((Group) scene.getRoot()).getChildren().addAll(vbox);
    stage.setScene(scene);
    stage.show();

    curator.getLimitList().addListener(new SpreadScroller());
  }

  private class SpreadScroller implements ListChangeListener<LimitView> {
    private Optional<LimitView> lastAsk = Optional.empty();

    @Override
    public void onChanged(Change<? extends LimitView> c) {
      Platform.runLater(() -> {

        Optional<LimitView> currentAsk = curator.getBestAsk();
        if (!lastAsk.isPresent() && currentAsk.isPresent()) {
          lastAsk = currentAsk;
          table.scrollTo(currentAsk.get());
        } else if (currentAsk.isPresent()) {
          lastAsk = currentAsk;
          table.scrollTo(currentAsk.get());
        }

      });
    }
  }

}
