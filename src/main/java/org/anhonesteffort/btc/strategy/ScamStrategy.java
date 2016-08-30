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

import org.anhonesteffort.btc.http.HttpClientWrapper;
import org.anhonesteffort.btc.http.request.model.PostOrderRequest;
import org.anhonesteffort.btc.state.State;
import org.anhonesteffort.btc.util.LongCaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ScamStrategy extends Strategy<Void> {

  private static final Logger log = LoggerFactory.getLogger(ScamStrategy.class);
  private final HttpClientWrapper http;
  private final LongCaster caster;

  private OrderOpeningStrategy    orderOpenStrategy;
  private BidIdentifyingStrategy  bidIdStrategy;
  private BidMatchingStrategy     bidMatchingStrategy;
  private AskIdentifyingStrategy  askIdStrategy;
  private AskMatchingStrategy     askMatchingStrategy;
  private ScamState               state;

  private enum ScamState {
    WAIT_TO_BID, BIDDING, MATCHING_BID,
    WAIT_TO_ASK, ASKING,  MATCHING_ASK,
    COMPLETE
  }

  public ScamStrategy(HttpClientWrapper http, LongCaster caster) {
    this.http   = http;
    this.caster = caster;
    state       = ScamState.COMPLETE;
  }

  @Override
  protected Void advanceStrategy(State state, long nanoseconds) throws StrategyException {
    switch (this.state) {
      case COMPLETE:
        log.info("awaiting buy opportunity");
        bidIdStrategy = new BidIdentifyingStrategy(caster);
        addChildren(bidIdStrategy);
        this.state = ScamState.WAIT_TO_BID;
        break;

      case WAIT_TO_BID:
        Optional<PostOrderRequest> bid = bidIdStrategy.getResult();
        if (bid.isPresent()) {
          log.info("opening bid for " + bid.get().getSize() + " at " + bid.get().getPrice());
          removeChildren(bidIdStrategy);
          orderOpenStrategy = new OrderOpeningStrategy(http, bid.get());
          addChildren(orderOpenStrategy);
          this.state = ScamState.BIDDING;
        }
        break;

      case BIDDING:
        Optional<String> bidId = orderOpenStrategy.getResult();
        if (bidId.isPresent()) {
          log.info("bid opened with id " + bidId.get() + ", waiting to match");
          removeChildren(orderOpenStrategy);
          bidMatchingStrategy = new BidMatchingStrategy(bidId.get());
          addChildren(bidMatchingStrategy);
          this.state = ScamState.MATCHING_BID;
        }
        break;

      case MATCHING_BID:
        if (bidMatchingStrategy.getResult()) {
          log.info("bid matched with ask, awaiting sell opportunity");
          removeChildren(bidMatchingStrategy);
          askIdStrategy = new AskIdentifyingStrategy();
          addChildren(askIdStrategy);
          this.state = ScamState.WAIT_TO_ASK;
        }
        break;

      case WAIT_TO_ASK:
        Optional<PostOrderRequest> ask = askIdStrategy.getResult();
        if (ask.isPresent()) {
          log.info("opening ask for " + ask.get().getSize() + " at " + ask.get().getPrice());
          removeChildren(askIdStrategy);
          orderOpenStrategy = new OrderOpeningStrategy(http, ask.get());
          addChildren(orderOpenStrategy);
          this.state = ScamState.ASKING;
        }
        break;

      case ASKING:
        Optional<String> askId = orderOpenStrategy.getResult();
        if (askId.isPresent()) {
          log.info("ask opened with id " + askId.get() + ", waiting to match");
          removeChildren(orderOpenStrategy);
          askMatchingStrategy = new AskMatchingStrategy(askId.get());
          addChildren(askMatchingStrategy);
          this.state = ScamState.MATCHING_ASK;
        }
        break;

      case MATCHING_ASK:
        if (askMatchingStrategy.getResult()) {
          log.info("ask matched with bid, strategy complete");
          removeChildren(askMatchingStrategy);
          this.state = ScamState.COMPLETE;
        }
        break;
    }

    return null;
  }

}
