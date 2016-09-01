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

package org.anhonesteffort.btc.strategy;

import org.anhonesteffort.btc.book.Order;
import org.anhonesteffort.btc.http.HttpClientWrapper;
import org.anhonesteffort.btc.http.request.model.PostOrderRequest;
import org.anhonesteffort.btc.state.State;
import org.anhonesteffort.btc.util.LongCaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ScamStrategy extends Strategy<Void> {

  private static final Long BID_ABORT_MS = 9_000l;
  private static final Long ASK_ABORT_MS = 1_800l;

  private static final Logger log = LoggerFactory.getLogger(ScamStrategy.class);
  private final HttpClientWrapper http;
  private final LongCaster caster;

  private OrderOpeningStrategy   openStrategy;
  private BidIdentifyingStrategy bidIdStrategy;
  private OrderMatchingStrategy  matchStrategy;
  private AskIdentifyingStrategy askIdStrategy;
  private OrderCancelingStrategy cancelStrategy;
  private Order                  bidPosition;
  private Order                  askPosition;
  private ScamState              state;

  private enum ScamState {
    IDENTIFY_BID, OPENING_BID, MATCHING_BID,
    IDENTIFY_ASK, OPENING_ASK, MATCHING_ASK,
    COMPLETE, ABORT
  }

  public ScamStrategy(HttpClientWrapper http, LongCaster caster) {
    this.http   = http;
    this.caster = caster;
    state       = ScamState.COMPLETE;
  }

  @Override
  protected Void advanceStrategy(State state, long nanoseconds) {
    switch (this.state) {
      case COMPLETE:
        log.info("awaiting buy opportunity");
        bidPosition   = null;
        askPosition   = null;
        bidIdStrategy = new BidIdentifyingStrategy(caster);
        addChildren(bidIdStrategy);
        this.state = ScamState.IDENTIFY_BID;
        break;

      case IDENTIFY_BID:
        Optional<PostOrderRequest> postBid = bidIdStrategy.getResult();
        if (postBid.isPresent()) {
          log.info("opening bid for " + postBid.get().getSize() + " at " + postBid.get().getPrice());
          removeChildren(bidIdStrategy);
          openStrategy = new OrderOpeningStrategy(http, postBid.get());
          addChildren(openStrategy);
          this.state = ScamState.OPENING_BID;
        }
        break;

      case OPENING_BID:
        Optional<Order> openedBid = openStrategy.getResult();
        if (openStrategy.isAborted()) {
          log.info("bid rejected due to post-only flag");
          removeChildren(openStrategy);
          this.state = ScamState.COMPLETE;
        } else if (openedBid.isPresent()) {
          bidPosition = openedBid.get();
          log.info("bid opened with id " + bidPosition.getOrderId() + ", waiting to match");
          removeChildren(openStrategy);
          matchStrategy = new OrderMatchingStrategy(bidPosition.getOrderId(), BID_ABORT_MS);
          addChildren(matchStrategy);
          this.state = ScamState.MATCHING_BID;
        }
        break;

      case MATCHING_BID:
        if (matchStrategy.isAborted()) {
          log.info("aborting bid due to match timeout");
          removeChildren(matchStrategy);
          cancelStrategy = new OrderCancelingStrategy(http, bidPosition.getOrderId());
          addChildren(cancelStrategy);
          this.state = ScamState.ABORT;
        } else if (matchStrategy.getResult()) {
          log.info("bid matched with ask, awaiting sell opportunity");
          removeChildren(matchStrategy);
          askIdStrategy = new AskIdentifyingStrategy(caster, bidPosition, Optional.empty());
          addChildren(askIdStrategy);
          this.state = ScamState.IDENTIFY_ASK;
        }
        break;

      case IDENTIFY_ASK:
        Optional<PostOrderRequest> postAsk = askIdStrategy.getResult();
        if (postAsk.isPresent()) {
          log.info("opening ask for " + postAsk.get().getSize() + " at " + postAsk.get().getPrice());
          removeChildren(askIdStrategy);
          openStrategy = new OrderOpeningStrategy(http, postAsk.get());
          addChildren(openStrategy);
          this.state = ScamState.OPENING_ASK;
        }
        break;

      case OPENING_ASK:
        Optional<Order> openedAsk = openStrategy.getResult();
        if (openStrategy.isAborted()) {
          log.info("ask rejected due to post-only flag");
          removeChildren(openStrategy);
          askIdStrategy = new AskIdentifyingStrategy(caster, bidPosition, Optional.empty());
          addChildren(askIdStrategy);
          this.state = ScamState.IDENTIFY_ASK;
        } else if (openedAsk.isPresent()) {
          askPosition = openedAsk.get();
          log.info("ask opened with id " + askPosition.getOrderId() + ", waiting to match");
          removeChildren(openStrategy);
          matchStrategy = new OrderMatchingStrategy(askPosition.getOrderId(), ASK_ABORT_MS);
          addChildren(matchStrategy);
          this.state = ScamState.MATCHING_ASK;
        }
        break;

      case MATCHING_ASK:
        if (matchStrategy.isAborted()) {
          log.info("aborting ask due to match timeout");
          removeChildren(matchStrategy);
          cancelStrategy = new OrderCancelingStrategy(http, askPosition.getOrderId());
          addChildren(cancelStrategy);
          this.state = ScamState.ABORT;
        } else if (matchStrategy.getResult()) {
          log.info("ask matched with bid, strategy complete");
          removeChildren(matchStrategy);
          this.state = ScamState.COMPLETE;
        }
        break;

      case ABORT:
        if (cancelStrategy.getResult() && askPosition == null) {
          log.info("bid " + bidPosition.getOrderId() + " canceled");
          removeChildren(cancelStrategy);
          this.state = ScamState.COMPLETE;
        } else if (cancelStrategy.getResult()) {
          log.info("ask " + askPosition.getOrderId() + " canceled");
          removeChildren(cancelStrategy);
          askIdStrategy = new AskIdentifyingStrategy(caster, bidPosition, Optional.of(askPosition));
          addChildren(askIdStrategy);
          this.state = ScamState.IDENTIFY_ASK;
        }
        break;
    }

    return null;
  }

}
