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

import org.anhonesteffort.btc.http.HttpClient;
import org.anhonesteffort.btc.ws.message.Message;
import org.anhonesteffort.btc.ws.WsErrorCallback;
import org.anhonesteffort.btc.ws.WsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CriticalCallback extends WsErrorCallback {

  private static final Logger log = LoggerFactory.getLogger(CriticalCallback.class);

  private final WsService wsService;

  public CriticalCallback(WsService wsService) {
    this.wsService = wsService;
  }

  private void stopWsService() {
    try {

      wsService.stop();
      HttpClient.getInstance().dispatcher().executorService().shutdownNow();
      log.info("application should cleanly exit now");

    } catch (IOException e) {
      log.error("unable to shutdown WsService cleanly", e);
      System.exit(1);
    }
  }

  @Override
  public void onSuccess(Void aVoid) {
    log.error("unknown ws error");
    stopWsService();
  }

  @Override
  public void onFailure(Throwable throwable) {
    log.error("ws error", throwable);
    stopWsService();
  }

  @Override
  public void handleEventException(Throwable throwable, long sequence, Message message) {
    log.error("error processing ws disruptor event", throwable);
    stopWsService();
  }

  @Override
  public void handleOnStartException(Throwable throwable) {
    log.error("error starting ws disruptor", throwable);
    stopWsService();
  }

  @Override
  public void handleOnShutdownException(Throwable throwable) {
    log.error("error stopping ws disruptor", throwable);
    stopWsService();
  }

}
