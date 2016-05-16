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

package org.anhonesteffort.btc.http;

import com.google.common.util.concurrent.SettableFuture;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;

public abstract class HttpCallback<T> implements Callback {

  protected final SettableFuture<T> future;

  protected HttpCallback(SettableFuture<T> future) {
    this.future = future;
  }

  protected abstract void set(Call call, Response response) throws Exception;

  @Override
  public void onResponse(Call call, Response response) {
    try {

      if (!response.isSuccessful()) {
        future.setException(new HttpException("http returned code " + response.code()));
      } else {
        set(call, response);
      }

    } catch (Throwable throwable) {
      future.setException(throwable);
    } finally {
      response.body().close();
    }
  }

  @Override
  public void onFailure(Call call, IOException e) {
    future.setException(e);
  }

}
