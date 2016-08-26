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

import okhttp3.Call;
import okhttp3.Response;
import org.anhonesteffort.btc.http.HttpCallback;

import java.util.concurrent.CompletableFuture;

public class PostOrderCallback extends HttpCallback<Response> {

  public PostOrderCallback(CompletableFuture<Response> future) {
    super(future);
  }

  @Override
  protected void set(Call call, Response response) {
    future.complete(response);
  }

}
