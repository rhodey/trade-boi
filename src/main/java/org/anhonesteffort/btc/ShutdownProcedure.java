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

import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShutdownProcedure implements Callable<Void> {

  private static final Logger log = LoggerFactory.getLogger(ShutdownProcedure.class);
  private final ExecutorService pool;
  private final WsService wsService;

  public ShutdownProcedure(ExecutorService pool, WsService wsService) {
    this.pool      = pool;
    this.wsService = wsService;
  }

  @Override
  public Void call() {
    AtomicBoolean shutdown = new AtomicBoolean(false);
    Scanner       console  = new Scanner(System.in);

    wsService.getShutdownFuture().whenComplete((ok, err) -> {
      if (!shutdown.getAndSet(true)) { pool.submit(new OrderClosingRunnable()); }
    });

    while (console.hasNextLine()) { console.nextLine(); }
    if (!shutdown.getAndSet(true)) { pool.submit(new OrderClosingRunnable()); }
    console.close();

    return null;
  }

  private static class OrderClosingRunnable implements Runnable {
    @Override
    public void run() {
      try {

        log.warn("shutdown procedure initiated");
        Thread.sleep(1000); // todo: close orders
        log.info("successfully closed open orders, exiting");

      } catch (Throwable e) {
        log.error("failed to close open orders, exiting", e);
      }

      System.exit(1);
    }
  }

}
