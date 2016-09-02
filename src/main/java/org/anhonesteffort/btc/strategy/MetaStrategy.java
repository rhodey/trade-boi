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
import org.anhonesteffort.btc.http.request.model.PostOrderRequest;
import org.anhonesteffort.btc.state.GdaxState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class MetaStrategy extends Strategy<Void> {

  private static final Logger log = LoggerFactory.getLogger(MetaStrategy.class);

  private final StrategyFactory        strategies;
  private final BidIdentifyingStrategy bidIdStrategy;
  private final AskIdentifyingStrategy askIdStrategy;

  private OrderOpeningStrategy   openStrategy;
  private OrderMatchingStrategy  matchStrategy;
  private OrderCancelingStrategy cancelStrategy;

  private State state;
  private Order bidPosition;
  private Order askPosition;

  private enum State {
    IDENTIFY_BID, OPENING_BID, MATCHING_BID,
    IDENTIFY_ASK, OPENING_ASK, MATCHING_ASK,
    COMPLETE, ABORT
  }

  public MetaStrategy(StrategyFactory strategies) {
    this.strategies = strategies;
    bidIdStrategy   = strategies.newBidIdentifying();
    askIdStrategy   = strategies.newAskIdentifying();
    state           = State.COMPLETE;

    addChildren(bidIdStrategy, askIdStrategy);
  }

  @Override
  protected Void advanceStrategy(GdaxState bookState, long nanoseconds) {
    switch (state) {
      case COMPLETE:
        log.info("awaiting buy opportunity");
        askIdStrategy.setContext(Optional.empty(), Optional.empty());
        bidPosition = null;
        askPosition = null;
        state       = State.IDENTIFY_BID;
        break;

      case IDENTIFY_BID:
        Optional<PostOrderRequest> postBid = bidIdStrategy.getResult();
        if (postBid.isPresent()) {
          log.info("opening bid for " + postBid.get().getSize() + " at " + postBid.get().getPrice());
          openStrategy = strategies.newOrderOpening(postBid.get());
          addChildren(openStrategy);
          state = State.OPENING_BID;
        }
        break;

      case OPENING_BID:
        Optional<Order> openedBid = openStrategy.getResult();
        if (openStrategy.isAborted()) {
          log.info("bid rejected due to post-only flag");
          removeChildren(openStrategy);
          state = State.IDENTIFY_BID;
        } else if (openedBid.isPresent()) {
          bidPosition = openedBid.get();
          log.info("bid opened with id " + bidPosition.getOrderId() + ", waiting to match");
          removeChildren(openStrategy);
          matchStrategy = strategies.newOrderMatching(Order.Side.BID, bidPosition.getOrderId());
          addChildren(matchStrategy);
          state = State.MATCHING_BID;
        }
        break;

      case MATCHING_BID:
        if (matchStrategy.isAborted()) {
          log.info("aborting bid due to match timeout");
          removeChildren(matchStrategy);
          cancelStrategy = strategies.newOrderCanceling(bidPosition.getOrderId());
          addChildren(cancelStrategy);
          state = State.ABORT;
        } else if (matchStrategy.getResult()) {
          log.info("bid matched with ask, awaiting sell opportunity");
          removeChildren(matchStrategy);
          askIdStrategy.setContext(Optional.of(bidPosition), Optional.empty());
          state = State.IDENTIFY_ASK;
        }
        break;

      case IDENTIFY_ASK:
        Optional<PostOrderRequest> postAsk = askIdStrategy.getResult();
        if (postAsk.isPresent()) {
          log.info("opening ask for " + postAsk.get().getSize() + " at " + postAsk.get().getPrice());
          openStrategy = strategies.newOrderOpening(postAsk.get());
          addChildren(openStrategy);
          state = State.OPENING_ASK;
        }
        break;

      case OPENING_ASK:
        Optional<Order> openedAsk = openStrategy.getResult();
        if (openStrategy.isAborted()) {
          log.info("ask rejected due to post-only flag");
          removeChildren(openStrategy);
          askIdStrategy.setContext(Optional.of(bidPosition), Optional.empty());
          state = State.IDENTIFY_ASK;
        } else if (openedAsk.isPresent()) {
          askPosition = openedAsk.get();
          log.info("ask opened with id " + askPosition.getOrderId() + ", waiting to match");
          removeChildren(openStrategy);
          matchStrategy = strategies.newOrderMatching(Order.Side.ASK, askPosition.getOrderId());
          addChildren(matchStrategy);
          state = State.MATCHING_ASK;
        }
        break;

      case MATCHING_ASK:
        if (matchStrategy.isAborted()) {
          log.info("aborting ask due to match timeout");
          removeChildren(matchStrategy);
          cancelStrategy = strategies.newOrderCanceling(askPosition.getOrderId());
          addChildren(cancelStrategy);
          state = State.ABORT;
        } else if (matchStrategy.getResult()) {
          log.info("ask matched with bid, strategy complete");
          removeChildren(matchStrategy);
          state = State.COMPLETE;
        }
        break;

      case ABORT:
        if (cancelStrategy.getResult() && askPosition == null) {
          log.info("bid " + bidPosition.getOrderId() + " canceled");
          removeChildren(cancelStrategy);
          state = State.IDENTIFY_BID;
        } else if (cancelStrategy.getResult()) {
          log.info("ask " + askPosition.getOrderId() + " canceled");
          removeChildren(cancelStrategy);
          askIdStrategy.setContext(Optional.of(bidPosition), Optional.of(askPosition));
          state = State.IDENTIFY_ASK;
        }
        break;
    }

    return null;
  }

}
