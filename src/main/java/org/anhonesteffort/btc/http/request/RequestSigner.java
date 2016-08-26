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

package org.anhonesteffort.btc.http.request;

import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

public class RequestSigner {

  private static final String HEADER_TIMESTAMP       = "CB-ACCESS-TIMESTAMP";
  private static final String HEADER_ACCESS_KEY      = "CB-ACCESS-KEY";
  private static final String HEADER_ACCESS_PASSWORD = "CB-ACCESS-PASSPHRASE";
  private static final String HEADER_ACCESS_SIGN     = "CB-ACCESS-SIGN";

  private final Mac    hmac;
  private final String accessKey;
  private final byte[] secretKey;
  private final String accessPassword;

  public RequestSigner(String accessKey, String secretKey, String accessPassword)
      throws NoSuchAlgorithmException
  {
    hmac                = Mac.getInstance("HmacSHA256");
    this.accessKey      = accessKey;
    this.secretKey      = Base64.getDecoder().decode(secretKey);
    this.accessPassword = accessPassword;
  }

  private String toString(RequestBody body) throws IOException {
    Buffer buffer = new Buffer();
    body.writeTo(buffer);
    return buffer.readUtf8();
  }

  private String signatureFor(String timestamp, String method, String path, Optional<RequestBody> body)
      throws IOException, InvalidKeyException, CloneNotSupportedException
  {
    String        plaintext = timestamp + method + path + (body.isPresent() ? toString(body.get()) : "");
    SecretKeySpec hmacSpec  = new SecretKeySpec(secretKey, "HmacSHA256");
    Mac           hmac      = (Mac) this.hmac.clone();

    hmac.init(hmacSpec);
    return Base64.getEncoder().encodeToString(hmac.doFinal(plaintext.getBytes()));
  }

  public void sign(Request.Builder builder, String method, String path, Optional<RequestBody> body)
      throws IOException, InvalidKeyException, CloneNotSupportedException
  {
    String timestamp = ((Long) Instant.now().getEpochSecond()).toString();

    builder.header(HEADER_TIMESTAMP,       timestamp)
           .header(HEADER_ACCESS_KEY,      accessKey)
           .header(HEADER_ACCESS_PASSWORD, accessPassword)
           .header(HEADER_ACCESS_SIGN,     signatureFor(timestamp, method, path, body));
  }

}
