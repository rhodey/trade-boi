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

import org.anhonesteffort.btc.netty.NettyWsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ShutdownProcedure implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(ShutdownProcedure.class);

  private final ExecutorService executor;
  private final NettyWsService  wsService;

  public ShutdownProcedure(ExecutorService executor, NettyWsService wsService) {
    this.executor  = executor;
    this.wsService = wsService;
  }

  @Override
  public void run() {
    try {

      wsService.shutdown();
      executor.submit(new OrderCloser()).get(10, TimeUnit.SECONDS);
      log.info("successfully closed open orders, exiting");

    } catch (Throwable e) {
      log.error("failed to close open orders, exiting", e);
    }

    System.exit(1);
  }

  private static class OrderCloser implements Callable<Void> {

    @Override
    public Void call() throws InterruptedException {
      // todo: close all open orders
      Thread.sleep(1000);
      return null;
    }

  }

}
