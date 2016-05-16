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

package org.anhonesteffort.btc;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.lmax.disruptor.BlockingWaitStrategy;
import org.anhonesteffort.btc.ws.WsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test implements Runnable, FutureCallback<Void> {

  private static final Logger  log            = LoggerFactory.getLogger(Test.class);
  private static final Integer WS_BUFFER_SIZE = 1024;

  @Override
  public void run() {
    WsService wsService = new WsService(new BlockingWaitStrategy(), WS_BUFFER_SIZE);
    Futures.addCallback(wsService.getShutdownFuture(), this);
    wsService.start();
  }

  @Override
  public void onSuccess(Void aVoid) {
    log.error("WsService shutdown with unknown cause");
    System.exit(1);
  }

  @Override
  public void onFailure(Throwable throwable) {
    log.error("WsService shutdown with error", throwable);
    System.exit(1);
  }

  public static void main(String[] args) {
    new Test().run();
  }

}
