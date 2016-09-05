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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ScamConfig {

  private final Double  precision;
  private final Double  accuracy;
  private final Integer limitInitSize;
  private final Integer wsBufferSize;
  private final Integer wsConnectTimeoutMs;
  private final Long    wsReadTimeoutMs;
  private final Boolean tradingEnabled;
  private final Boolean gdaxSandbox;
  private final String  gdaxAccessKey;
  private final String  gdaxSecretKey;
  private final String  gdaxPassword;
  private final Boolean persistenceEnabled;
  private final String  persistenceDir;
  private final Boolean statsEnabled;
  private final Integer statsPort;

  public ScamConfig() throws IOException {
    Properties properties = new Properties();
    properties.load(new FileInputStream("scam.properties"));

    precision          = Double.parseDouble(properties.getProperty("precision"));
    accuracy           = Double.parseDouble(properties.getProperty("accuracy"));
    limitInitSize      = Integer.parseInt(properties.getProperty("limit_init_size"));
    wsBufferSize       = Integer.parseInt(properties.getProperty("ws_buffer_size"));
    wsConnectTimeoutMs = Integer.parseInt(properties.getProperty("ws_connect_timeout_ms"));
    wsReadTimeoutMs    = Long.parseLong(properties.getProperty("ws_read_timeout_ms"));
    tradingEnabled     = Boolean.parseBoolean(properties.getProperty("trading_enabled"));
    gdaxSandbox        = Boolean.parseBoolean(properties.getProperty("gdax_sandbox"));
    gdaxAccessKey      = properties.getProperty("gdax_access_key");
    gdaxSecretKey      = properties.getProperty("gdax_secret_key");
    gdaxPassword       = properties.getProperty("gdax_key_password");
    persistenceEnabled = Boolean.parseBoolean(properties.getProperty("persistence_enabled"));
    persistenceDir     = properties.getProperty("persistence_dir");
    statsEnabled       = Boolean.parseBoolean(properties.getProperty("stats_enabled"));
    statsPort          = Integer.parseInt(properties.getProperty("stats_port"));
  }

  public Double getPrecision() {
    return precision;
  }

  public Double getAccuracy() {
    return accuracy;
  }

  public Integer getLimitInitSize() {
    return limitInitSize;
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

  public Boolean getTradingEnabled() {
    return tradingEnabled;
  }

  public Boolean getGdaxSandbox() {
    return gdaxSandbox;
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

  public Boolean getPersistenceEnabled() {
    return persistenceEnabled;
  }

  public String getPersistenceDir() {
    return persistenceDir;
  }

  public Boolean getStatsEnabled() {
    return statsEnabled;
  }

  public Integer getStatsPort() {
    return statsPort;
  }

}
