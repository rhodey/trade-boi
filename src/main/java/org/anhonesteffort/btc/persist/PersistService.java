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

import org.anhonesteffort.btc.ScamConfig;
import org.anhonesteffort.btc.Service;
import org.anhonesteffort.btc.state.StateListener;

import java.util.concurrent.CompletableFuture;

public class PersistService implements Service {

  private final CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();
  private final StatePersistingChronicle[] chronicles;

  public PersistService(ScamConfig config) {
    if (config.getPersistenceEnabled()) {
      chronicles = new StatePersistingChronicle[] {
          new OrderEventPersistingChronicle(config.getPersistenceDir())
      };
    } else {
      chronicles = new StatePersistingChronicle[0];
    }
  }

  public StateListener[] listeners() {
    return chronicles;
  }

  @Override
  public CompletableFuture<Void> shutdownFuture() {
    return shutdownFuture;
  }

  @Override
  public void start() { }

  @Override
  public boolean shutdown() {
    if (shutdownFuture.complete(null)) {
      for (StatePersistingChronicle chronicle : chronicles) { chronicle.close(); }
      return true;
    } else {
      return false;
    }
  }

}
