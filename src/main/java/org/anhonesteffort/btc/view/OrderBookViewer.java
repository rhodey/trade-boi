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

import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.anhonesteffort.btc.book.LimitOrderBook;
import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.util.LongCaster;

public class OrderBookViewer {

  private final LimitListCurator curator;

  public OrderBookViewer(LimitOrderBook orderBook, LongCaster caster) {
    curator = new LimitListCurator(orderBook, caster);
  }

  @SuppressWarnings("unchecked")
  private VBox vBoxFor(Order.Side side) {
    VBox                 vbox      = new VBox();
    TableView<LimitView> table     = new TableView<>();
    TableColumn          priceCol  = new TableColumn("price");
    TableColumn          volumeCol = new TableColumn("volume");

    priceCol.setMinWidth(100);
    volumeCol.setMinWidth(100);
    priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
    volumeCol.setCellValueFactory(new PropertyValueFactory<>("volume"));

    table.setEditable(true);
    table.setItems(side.equals(Order.Side.ASK) ? curator.getAskList() : curator.getBidList());
    table.getColumns().addAll(volumeCol, priceCol);

    Label label = new Label((side.equals(Order.Side.ASK) ? "Ask" : "Bid") + " Limit Orders");
    label.setFont(new Font("Arial", 20));

    vbox.setSpacing(5);
    vbox.setPadding(new Insets(10, 0, 0, 10));
    vbox.getChildren().addAll(label, table);

    return vbox;
  }

  public void start(Stage stage) {
    stage.setTitle("Coinbase Trading");
    stage.setWidth(300);
    stage.setHeight(500);

    Scene scene = new Scene(new Group());

    ((Group) scene.getRoot()).getChildren().addAll(
        vBoxFor(Order.Side.ASK)/*, vBoxFor(Order.Side.BID)*/
    );
    stage.setScene(scene);
    stage.show();
  }

}
