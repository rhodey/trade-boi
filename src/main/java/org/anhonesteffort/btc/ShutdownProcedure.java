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

import org.anhonesteffort.btc.ws.WsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownProcedure implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(ShutdownProcedure.class);
  private final WsService wsService;

  public ShutdownProcedure(WsService wsService) {
    this.wsService = wsService;
  }

  @Override
  public void run() {
    try {

      log.warn("shutdown procedure initiated");
      wsService.shutdown();
      Thread.sleep(1000); // todo: close orders
      log.info("successfully closed open orders, exiting");

    } catch (Throwable e) {
      log.error("failed to close open orders, exiting", e);
    }

    System.exit(1);
  }

}
