package org.anhonesteffort.trading.strategy
import org.anhonesteffort.trading.book.Orders.{Order, Side}
import org.anhonesteffort.trading.state.GdaxState
import org.slf4j.{Logger, LoggerFactory}

class MetaStrategy(strategies: StrategyFactory) extends Strategy[Unit] {

  private val log: Logger = LoggerFactory.getLogger(classOf[MetaStrategy])

  sealed trait State
  object State {
    case object IDENTIFY_BID extends State
    case object OPENING_BID  extends State
    case object MATCHING_BID extends State
    case object IDENTIFY_ASK extends State
    case object OPENING_ASK  extends State
    case object MATCHING_ASK extends State
    case object COMPLETE     extends State
    case object ABORT        extends State
  }

  private val bidIdStrategy : BidIdentifyingStrategy = strategies.newBidIdentifying
  private val askIdStrategy : AskIdentifyingStrategy = strategies.newAskIdentifying

  private var openStrategy   : OrderOpeningStrategy   = _
  private var matchStrategy  : OrderMatchingStrategy  = _
  private var cancelStrategy : OrderCancelingStrategy = _

  private var state       : State = State.COMPLETE
  private var bidPosition : Order = _
  private var askPosition : Order = _

  addChildren(Seq(bidIdStrategy, askIdStrategy))

  override protected def advanceStrategy(bookState: GdaxState, ns: Long): Unit = {
    state match {
      case State.COMPLETE =>
        log.info("awaiting buy opportunity")
        askIdStrategy.setContext(None, None)
        bidPosition = null
        askPosition = null
        state       = State.IDENTIFY_BID

      case State.IDENTIFY_BID =>
        bidIdStrategy.getResult match {
          case None      => Unit
          case Some(bid) =>
            log.info(s"opening bid for ${bid.getSize} at ${bid.getPrice}")
            openStrategy = strategies.newOrderOpening(bid)
            addChild(openStrategy)
            state = State.OPENING_BID
        }

      case State.OPENING_BID =>
        val bid = openStrategy.getResult
        if (openStrategy.isAborted) {
          log.info("bid rejected due to post-only flag")
          removeChild(openStrategy)
          state = State.IDENTIFY_BID
        } else if (bid.isDefined) {
          bidPosition = bid.get
          log.info(s"bid opened with id ${bidPosition.getOrderId}, waiting to match")
          removeChild(openStrategy)
          matchStrategy = strategies.newOrderMatching(Side.BID, bidPosition.getOrderId)
          addChild(matchStrategy)
          state = State.MATCHING_BID
        }

      case State.MATCHING_BID =>
        if (matchStrategy.isAborted) {
          log.info("aborting bid due to match timeout")
          removeChild(matchStrategy)
          cancelStrategy = strategies.newOrderCanceling(bidPosition.getOrderId)
          addChild(cancelStrategy)
          state = State.ABORT
        } else if (matchStrategy.getResult) {
          log.info("bid matched with ask, awaiting sell opportunity")
          removeChild(matchStrategy)
          askIdStrategy.setContext(Some(bidPosition), None)
          state = State.IDENTIFY_ASK
        }

      case State.IDENTIFY_ASK =>
        askIdStrategy.getResult match {
          case None      => Unit
          case Some(ask) =>
            log.info(s"opening ask for ${ask.getSize} at ${ask.getPrice}")
            openStrategy = strategies.newOrderOpening(ask)
            addChild(openStrategy)
            state = State.OPENING_ASK
        }

      case State.OPENING_ASK =>
        val ask = openStrategy.getResult
        if (openStrategy.isAborted) {
          log.info("ask rejected due to post-only flag")
          removeChild(openStrategy)
          askIdStrategy.setContext(Some(bidPosition), None)
          state = State.IDENTIFY_ASK
        } else if (ask.isDefined) {
          askPosition = ask.get
          log.info(s"ask opened with id ${askPosition.getOrderId}, waiting to match")
          removeChild(openStrategy)
          matchStrategy = strategies.newOrderMatching(Side.ASK, askPosition.getOrderId)
          addChild(matchStrategy)
          state = State.MATCHING_ASK
        }

      case State.MATCHING_ASK =>
        if (matchStrategy.isAborted) {
          log.info("aborting ask due to match timeout")
          removeChild(matchStrategy)
          cancelStrategy = strategies.newOrderCanceling(askPosition.getOrderId)
          addChild(cancelStrategy)
          state = State.ABORT
        } else if (matchStrategy.getResult) {
          log.info("ask matched with bid, strategy complete")
          removeChild(matchStrategy)
          state = State.COMPLETE
        }

      case State.ABORT =>
        if (cancelStrategy.getResult && askPosition == null) {
          log.info("bid " + bidPosition.getOrderId + " canceled")
          removeChild(cancelStrategy)
          state = State.IDENTIFY_BID
        } else if (cancelStrategy.getResult) {
          log.info("ask " + askPosition.getOrderId + " canceled")
          removeChild(cancelStrategy)
          askIdStrategy.setContext(Some(bidPosition), Some(askPosition))
          state = State.IDENTIFY_ASK
        }
    }
  }

}
