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

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ChronicleQueueBuilder;
import net.openhft.chronicle.queue.ExcerptAppender;
import org.anhonesteffort.btc.state.StateListener;

import java.io.Closeable;

public abstract class StatePersistingChronicle implements StateListener, Closeable {

  private   final ChronicleQueue queue;
  protected final ExcerptAppender appender;

  public StatePersistingChronicle(String fsPath) {
    queue    = ChronicleQueueBuilder.single(fsPath).build();
    appender = queue.acquireAppender();
  }

  @Override
  public void close() {
    queue.close();
  }

  @Override
  public void onStateReset() { }

}
