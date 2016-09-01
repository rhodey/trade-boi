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

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WaitStrategy;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ScamConfig {

  private final Double  precision;
  private final Integer limitInitSize;
  private final Boolean useSandbox;
  private final Integer wsBufferSize;
  private final Integer wsConnectTimeoutMs;
  private final Long    wsReadTimeoutMs;
  private final String  gdaxAccessKey;
  private final String  gdaxSecretKey;
  private final String  gdaxPassword;
  private final Boolean statsEnabled;
  private final Integer statsPort;

  private WaitStrategy   waitStrategy;
  private EventHandler[] eventHandlers;

  public ScamConfig() throws IOException {
    Properties properties = new Properties();
    properties.load(new FileInputStream("scam.properties"));

    precision          = Double.parseDouble(properties.getProperty("precision"));
    limitInitSize      = Integer.parseInt(properties.getProperty("limit_init_size"));
    useSandbox         = Boolean.parseBoolean(properties.getProperty("use_sandbox"));
    wsBufferSize       = Integer.parseInt(properties.getProperty("ws_buffer_size"));
    wsConnectTimeoutMs = Integer.parseInt(properties.getProperty("ws_connect_timeout_ms"));
    wsReadTimeoutMs    = Long.parseLong(properties.getProperty("ws_read_timeout_ms"));
    gdaxAccessKey      = properties.getProperty("gdax_access_key");
    gdaxSecretKey      = properties.getProperty("gdax_secret_key");
    gdaxPassword       = properties.getProperty("gdax_key_password");
    statsEnabled       = Boolean.parseBoolean(properties.getProperty("stats_enabled"));
    statsPort          = Integer.parseInt(properties.getProperty("stats_port"));
  }

  public Double getPrecision() {
    return precision;
  }

  public Integer getLimitInitSize() {
    return limitInitSize;
  }

  public Boolean getUseSandbox() {
    return useSandbox;
  }

  public Integer getWsBufferSize() {
    return wsBufferSize;
  }

  public Integer getWsConnectTimeoutMs() {
    return wsConnectTimeoutMs;
  }

  public Long getWsReadTimeoutMs() {
    return wsReadTimeoutMs;
  }

  public String getGdaxAccessKey() {
    return gdaxAccessKey;
  }

  public String getGdaxSecretKey() {
    return gdaxSecretKey;
  }

  public String getGdaxPassword() {
    return gdaxPassword;
  }

  public Boolean getStatsEnabled() {
    return statsEnabled;
  }

  public Integer getStatsPort() {
    return statsPort;
  }

  public WaitStrategy getWaitStrategy() {
    return waitStrategy;
  }

  protected void setWaitStrategy(WaitStrategy waitStrategy) {
    this.waitStrategy = waitStrategy;
  }

  public EventHandler[] getEventHandlers() {
    return eventHandlers;
  }

  protected void setEventHandlers(EventHandler[] eventHandlers) {
    this.eventHandlers = eventHandlers;
  }

}
