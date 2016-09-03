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
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import net.openhft.chronicle.wire.DocumentContext;
import org.anhonesteffort.trading.book.OrderEvent;

import java.io.IOException;

public class Test {

  public static void main(String[] args) {
    try (ChronicleQueue queue = SingleChronicleQueueBuilder.binary("persistence").build()) {
      ExcerptTailer trailer = queue.createTailer();

      while (true) {
        try (DocumentContext context = trailer.readingDocument()) {

          if (context.isPresent()) {
            OrderEvent order = new OrderEvent();
            order.readExternal(context.wire().objectInput());
            System.out.println(order.getType() + " -> " + order.getOrderId());
          } else {
            Thread.sleep(100l);
          }

        } catch (IOException | InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

}
