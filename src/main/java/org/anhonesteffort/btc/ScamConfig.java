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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ScamConfig {

  private final Integer wsBufferSize;
  private final Integer wsConnectTimeoutMs;
  private final Integer wsReadTimeoutMs;
  private final Integer limitInitSize;
  private final String  coinbaseAccessKey;
  private final String  coinbaseSecretKey;
  private final String  coinbaseKeyPassword;

  public ScamConfig() throws IOException {
    Properties properties = new Properties();
    properties.load(new FileInputStream("scam.properties"));

    wsBufferSize        = Integer.parseInt(properties.getProperty("ws_buffer_size"));
    wsConnectTimeoutMs  = Integer.parseInt(properties.getProperty("ws_connect_timeout_ms"));
    wsReadTimeoutMs     = Integer.parseInt(properties.getProperty("ws_read_timeout_ms"));
    limitInitSize       = Integer.parseInt(properties.getProperty("limit_initial_size"));
    coinbaseAccessKey   = properties.getProperty("coinbase_access_key");
    coinbaseSecretKey   = properties.getProperty("coinbase_secret_key");
    coinbaseKeyPassword = properties.getProperty("coinbase_key_password");
  }

  public Integer getWsBufferSize() {
    return wsBufferSize;
  }

  public Integer getWsConnectTimeoutMs() {
    return wsConnectTimeoutMs;
  }

  public Integer getWsReadTimeoutMs() {
    return wsReadTimeoutMs;
  }

  public Integer getLimitInitSize() {
    return limitInitSize;
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

}
