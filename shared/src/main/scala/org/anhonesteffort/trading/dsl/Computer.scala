package org.anhonesteffort.trading.dsl

import org.anhonesteffort.trading.book.Orders
import org.anhonesteffort.trading.compute.{Computation, SpreadComputation, SummingComputation, TakeVolumeComputation}
import org.anhonesteffort.trading.state.StateListener

class Computer(computation: Ast.Computation) {

  private type Compute       = Computation[Double]
  private type ComputeOption = Computation[Option[Double]]

  private def instantiateCompute(computation: Ast.Computation): Compute = {
    computation match {
      case take: Ast.Computation.TakeVolume => take.side match {
        case Ast.Side.Bid => new TakeVolumeComputation(Orders.Side.BID)
        case Ast.Side.Ask => new TakeVolumeComputation(Orders.Side.ASK)
      }
      case _ => throw new IllegalArgumentException(s"computation $computation does not compute Double")
    }
  }

  private def instantiateEither(computation: Ast.Computation): Either[Compute, ComputeOption] = {
    computation match {
      case       Ast.Computation.Spread     => Right(new SpreadComputation)
      case take: Ast.Computation.TakeVolume => Left(instantiateCompute(take))
      case  sum: Ast.Computation.Sum        => Right(new SummingComputation(
        instantiateCompute(sum.child), sum.period.milliseconds
      ))
    }
  }

  private val instance: Either[Compute, ComputeOption] = instantiateEither(computation)

  val listener: StateListener = instance match {
    case Left(left)   => left
    case Right(right) => right
  }

  def compute(): Option[Double] = {
    instance match {
      case Left(computer)  => Some(computer.getResult)
      case Right(computer) => computer.getResult match {
        case Some(result)  => Some(result)
        case None          => None
      }
    }
  }

}
