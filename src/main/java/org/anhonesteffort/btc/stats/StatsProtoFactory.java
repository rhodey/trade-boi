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

package org.anhonesteffort.btc.stats;

import static org.anhonesteffort.btc.stats.StatsProto.BaseMessage;
import static org.anhonesteffort.btc.stats.StatsProto.Error;

public class StatsProtoFactory {

  public BaseMessage error(String message) {
    return BaseMessage.newBuilder().setType(BaseMessage.Type.ERROR).setError(
        Error.newBuilder().setMessage(message)
    ).build();
  }

}
