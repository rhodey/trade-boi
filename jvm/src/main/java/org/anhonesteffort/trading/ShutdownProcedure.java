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

package org.anhonesteffort.trading;

import org.anhonesteffort.trading.dsl.Runtime.DslContext;
import org.anhonesteffort.trading.http.HttpClientWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShutdownProcedure implements Callable<Void> {

  private static final Logger log = LoggerFactory.getLogger(ShutdownProcedure.class);

  private final ExecutorService pool = Executors.newFixedThreadPool(1);
  private final AtomicBoolean shutdown = new AtomicBoolean(false);
  private final HttpClientWrapper http;
  private final Optional<DslContext> dsl;
  private final Service[] services;

  public ShutdownProcedure(HttpClientWrapper http, Optional<DslContext> dsl, Service... services) {
    this.http     = http;
    this.dsl      = dsl;
    this.services = services;
  }

  private void shutdown(Throwable error) {
    if (!shutdown.getAndSet(true)) {
      if (error != null) {
        log.error("initiating shutdown procedure", error);
      } else {
        log.info("initiating shutdown procedure");
      }

      http.close();
      for (Service service : services) { service.shutdown(); }
      pool.submit(new OrderClosingRunnable());
    }
  }

  @Override
  public Void call() {
    for (Service service : services) {
      service.shutdownFuture().whenComplete((ok, err) -> shutdown(err));
    }

    try (Scanner console = new Scanner(System.in)) {

      while (console.hasNextLine()) {
        if (dsl.isPresent()) {
          dsl.get().eval(console.nextLine());
        } else {
          console.nextLine();
        }
      }
      shutdown(null);

    } catch (Throwable err) {
      shutdown(err);
    }

    return null;
  }

  private class OrderClosingRunnable implements Runnable {
    @Override
    public void run() {
      try {

        http.cancelAllOrders().whenComplete((ok, err) -> {
          if (err == null) {
            log.info("successfully canceled open orders");
            System.exit(0);
          } else {
            log.error("failed to cancel open orders", err);
            System.exit(1);
          }
        });

      } catch (Throwable e) {
        log.error("failed to cancel open orders", e);
        System.exit(1);
      }
    }
  }

}
