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

import com.lmax.disruptor.BlockingWaitStrategy;
import org.anhonesteffort.btc.ws.WsService;

public class Test implements Runnable {

  private static final Integer WS_BUFFER_SIZE = 1024;

  private final WsService        wsService;
  private final CriticalCallback criticalCb;

  public Test() {
    wsService  = new WsService(new BlockingWaitStrategy(), WS_BUFFER_SIZE);
    criticalCb = new CriticalCallback(wsService);
  }

  @Override
  public void run() {
    wsService.start(criticalCb);
  }

  public static void main(String[] args) {
    new Test().run();
  }

}
