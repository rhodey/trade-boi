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

package org.anhonesteffort.btc.http.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import okhttp3.Call;
import okhttp3.Response;
import org.anhonesteffort.trading.http.HttpCallback;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class PostOrderCallback extends HttpCallback<Boolean> {

  private final ObjectReader reader;

  public PostOrderCallback(ObjectReader reader, CompletableFuture<Boolean> future) {
    super(future);
    this.reader = reader;
  }

  @Override
  protected void complete(Call call, Response response) {
    try {

      JsonNode root = reader.readTree(response.body().charStream());
      if (root.get("status") != null && root.get("status").isTextual()) {
        future.complete(!root.get("status").textValue().equals("rejected"));
      } else {
        future.complete(true);
      }

    } catch (IOException e) {
      future.complete(false);
    }
  }

}
