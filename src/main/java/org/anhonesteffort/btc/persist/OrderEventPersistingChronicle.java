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

package org.anhonesteffort.btc.persist;

import net.openhft.chronicle.wire.DocumentContext;
import org.anhonesteffort.btc.state.GdaxState;
import org.anhonesteffort.btc.state.StateProcessingException;

import java.io.IOException;

public class OrderEventPersistingChronicle extends StatePersistingChronicle {

  public OrderEventPersistingChronicle(String fsPath) {
    super(fsPath);
  }

  @Override
  public void onStateChange(GdaxState state, long nanoseconds) throws StateProcessingException {
    if (state.getEvent().isPresent()) {
      try (DocumentContext context = appender.writingDocument()) {
        state.getEvent().get().writeExternal(context.wire().objectOutput());
      } catch (IOException e) {
        throw new StateProcessingException("error writing OrderEvent to chronicle", e);
      }
    }
  }

}
