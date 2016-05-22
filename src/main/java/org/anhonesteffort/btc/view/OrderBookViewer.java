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

import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import java.util.Timer;
import java.util.TimerTask;

public class OrderBookViewer {

  private static final Integer WIDTH        =  300;
  private static final Integer HEIGHT       =  600;
  private static final Integer COL_WIDTH    =  150;
  private static final Long    SCROLL_DELAY = 3000l;

  private final Timer timer = new Timer(true);
  private final TableView<LimitView> table = new TableView<>();
  private final LimitListCurator curator;

  public OrderBookViewer(LimitOrderBook orderBook, LongCaster caster) {
    curator = new LimitListCurator(orderBook, caster);
  }

  @SuppressWarnings("unchecked")
  public void start(Stage stage) {
    stage.setTitle("Coinbase Trading");
    stage.setWidth(WIDTH);
    stage.setHeight(HEIGHT);

    TableColumn priceCol  = new TableColumn("price");
    TableColumn volumeCol = new TableColumn("volume");

    priceCol.setMinWidth(COL_WIDTH);
    volumeCol.setMinWidth(COL_WIDTH);
    priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
    volumeCol.setCellValueFactory(new PropertyValueFactory<>("volume"));

    table.setItems(curator.getLimitList());
    table.getColumns().addAll(volumeCol, priceCol);
    table.setPrefHeight(HEIGHT);

    Scene scene = new Scene(new Group());
    VBox  vbox  = new VBox();

    vbox.setSpacing(5);
    vbox.setPadding(new Insets(0, 0, 0, 0));
    vbox.getChildren().addAll(table);

    ((Group) scene.getRoot()).getChildren().addAll(vbox);
    stage.setScene(scene);
    stage.show();

    table.getSelectionModel().selectedIndexProperty().addListener(new SelectedScroller());
    timer.schedule(new SpreadSelector(), SCROLL_DELAY);
  }

  private class SelectedScroller implements ChangeListener<Number> {
    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
      Platform.runLater(() -> {
        TableViewSkin<?> ts = (TableViewSkin<?>) table.getSkin();
        VirtualFlow<?>   vf = (VirtualFlow<?>)   ts.getChildren().get(1);

        if (vf.getFirstVisibleCellWithinViewPort() != null &&
            vf.getLastVisibleCellWithinViewPort()  != null)
        {
          int first = vf.getFirstVisibleCellWithinViewPort().getIndex();
          int last  = vf.getLastVisibleCellWithinViewPort().getIndex();

          if ((newValue.intValue() - ((last - first) / 2)) >= 0) {
            vf.scrollTo(newValue.intValue() - ((last - first) / 2));
          }
        }
      });
    }
  }

  private class SpreadSelector extends TimerTask implements ListChangeListener<LimitView> {
    private LimitView lastLimit      = null;
    private Integer   lastLimitCount = null;

    private void setLast(LimitView limit, Integer limitCount) {
      lastLimit      = limit;
      lastLimitCount = limitCount;
      table.getSelectionModel().select(limit);
    }

    @Override
    public void onChanged(Change<? extends LimitView> c) {
      Platform.runLater(() -> {
        Optional<LimitView> currentLimit      = curator.getBestAsk();
        Integer             currentLimitCount = curator.getLimitList().size();

        if (lastLimit == null && currentLimit.isPresent()) {
          setLast(currentLimit.get(), currentLimitCount);
        } else if (currentLimit.isPresent() && currentLimit.get().getPrice() != lastLimit.getPrice()) {
          setLast(currentLimit.get(), currentLimitCount);
        } else if (currentLimit.isPresent() && !lastLimitCount.equals(currentLimitCount)) {
          setLast(currentLimit.get(), currentLimitCount);
        }
      });
    }

    @Override
    public void run() {
      curator.getLimitList().addListener(this);
    }
  }

}
