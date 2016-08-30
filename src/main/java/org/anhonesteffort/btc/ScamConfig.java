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
import com.lmax.disruptor.WaitStrategy;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ScamConfig {

  private final Integer limitInitSize;
  private final Integer wsBufferSize;
  private final Integer wsConnectTimeoutMs;
  private final Long    wsReadTimeoutMs;
  private final String  coinbaseAccessKey;
  private final String  coinbaseSecretKey;
  private final String  coinbaseKeyPassword;
  private final Boolean statsEnabled;
  private final Integer statsPort;

  public ScamConfig() throws IOException {
    Properties properties = new Properties();
    properties.load(new FileInputStream("scam.properties"));

    limitInitSize       = Integer.parseInt(properties.getProperty("limit_initial_size"));
    wsBufferSize        = Integer.parseInt(properties.getProperty("ws_buffer_size"));
    wsConnectTimeoutMs  = Integer.parseInt(properties.getProperty("ws_connect_timeout_ms"));
    wsReadTimeoutMs     = Long.parseLong(properties.getProperty("ws_read_timeout_ms"));
    coinbaseAccessKey   = properties.getProperty("coinbase_access_key");
    coinbaseSecretKey   = properties.getProperty("coinbase_secret_key");
    coinbaseKeyPassword = properties.getProperty("coinbase_key_password");
    statsEnabled        = Boolean.parseBoolean(properties.getProperty("stats_enabled"));
    statsPort           = Integer.parseInt(properties.getProperty("stats_port"));
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

  public WaitStrategy getWaitStrategy() {
    return new BlockingWaitStrategy();
  }

  public String getCoinbaseAccessKey() {
    return coinbaseAccessKey;
  }

  public String getCoinbaseSecretKey() {
    return coinbaseSecretKey;
  }

  public String getCoinbaseKeyPassword() {
    return coinbaseKeyPassword;
  }

  public Boolean getStatsEnabled() {
    return statsEnabled;
  }

  public Integer getStatsPort() {
    return statsPort;
  }

}
