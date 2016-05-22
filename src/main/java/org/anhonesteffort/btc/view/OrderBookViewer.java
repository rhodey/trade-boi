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
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class OrderBookViewer {

  private final TableView table = new TableView();

  @SuppressWarnings("unchecked")
  public void start(Stage stage) {
    stage.setTitle("Table View Sample");
    stage.setWidth(300);
    stage.setHeight(500);

    TableColumn firstNameCol = new TableColumn("First Name");
    TableColumn lastNameCol  = new TableColumn("Last Name");
    TableColumn emailCol     = new TableColumn("Email");

    table.setEditable(true);
    table.getColumns().addAll(firstNameCol, lastNameCol, emailCol);

    Label label = new Label("Address Book");
    label.setFont(new Font("Arial", 20));

    VBox vbox = new VBox();
    vbox.setSpacing(5);
    vbox.setPadding(new Insets(10, 0, 0, 10));
    vbox.getChildren().addAll(label, table);

    Scene scene = new Scene(new Group());
    ((Group) scene.getRoot()).getChildren().addAll(vbox);

    stage.setScene(scene);
    stage.show();
  }

}
