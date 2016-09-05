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

package org.anhonesteffort.trading.http.response;

import com.fasterxml.jackson.databind.ObjectReader;
import okhttp3.Call;
import okhttp3.Response;
import org.anhonesteffort.trading.http.response.model.GetOrderBookResponse;
import org.anhonesteffort.trading.http.HttpCallback;
import org.anhonesteffort.trading.http.HttpException;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class GetOrderBookCallback extends HttpCallback<GetOrderBookResponse> {

  private final ObjectReader reader;

  public GetOrderBookCallback(ObjectReader reader, CompletableFuture<GetOrderBookResponse> future) {
    super(future);
    this.reader = reader;
  }

  @Override
  protected void complete(Call call, Response response) throws IOException, HttpException {
    future.complete(new GetOrderBookResponse(
        reader.readTree(response.body().charStream())
    ));
  }

}
