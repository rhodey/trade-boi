package org.anhonesteffort.trading.dsl

import org.anhonesteffort.trading.state.{GdaxState, StateListener}

import scala.collection.mutable

case class Evaluator(ctx: Runtime.Context, exp: Ast.Expression) extends StateListener {

  private val COMPUTERS = mutable.HashMap[Ast.Computation, Computer]()

  private def eval(computation: Ast.Computation): Runtime.EvalOption = {
    if (!COMPUTERS.contains(computation)) {
      COMPUTERS.put(computation, new Computer(computation))
    }

    COMPUTERS.get(computation) match {
      case Some(computer) => computer.compute() match {
        case Some(result) => Some(Right(result))
        case None         => None
      }
      case None => throw new RuntimeException(s"unable to find computer for $computation")
    }
  }

  private def evalOperand(operand: Ast.Expression): Runtime.EvalOption = {
    operand match {
      case bool:    Ast.Expression.Bool     => Some(Left(bool.value))
      case num:     Ast.Expression.Number   => Some(Right(num.value))
      case read:    Ast.Expression.VarRead  => ctx.readVar(read.name)
      case compute: Ast.Computation         => eval(compute)
      case _ => throw new IllegalArgumentException("evalOperand() called with non-operand")
    }
  }

  private def eval(left: Ast.Expression, right: Ast.Expression): Option[(Runtime.EvalResult, Runtime.EvalResult)] = {
    (evalOperand(left), evalOperand(right)) match {
      case (Some(l), Some(r)) => Some(l, r)
      case _ => None
    }
  }

  private def eval(operation: Ast.Expression.Operation): Runtime.EvalOption = {
    eval(operation.left, operation.right) match {
      case Some((left, right)) =>
        if (left.isLeft || right.isLeft) {
          throw new RuntimeException("cannot perform +-*/ operations on boolean types")
        } else {
          operation.op match {
            case Ast.Operator.Add      => Some(Right(left.right.get + right.right.get))
            case Ast.Operator.Subtract => Some(Right(left.right.get - right.right.get))
            case Ast.Operator.Multiply => Some(Right(left.right.get * right.right.get))
            case Ast.Operator.Divide   => Some(Right(left.right.get / right.right.get))
          }
        }

      case None => None
    }
  }

  private def eval(comparison: Ast.Expression.Comparison): Runtime.EvalOption = {
    eval(comparison.left, comparison.right) match {
      case Some((left, right)) =>
        if (left.isLeft && right.isLeft) {
          comparison.comp match {
            case Ast.BoolComparator.Equal    => Some(Left(left.left.get == right.left.get))
            case Ast.BoolComparator.NotEqual => Some(Left(left.left.get != right.left.get))
            case _ => throw new RuntimeException("can only compare boolean types using == and !=")
          }
        } else if (left.isRight && right.isRight) {
          comparison.comp match {
            case Ast.BoolComparator.Equal          => Some(Left(left.right.get == right.right.get))
            case Ast.BoolComparator.NotEqual       => Some(Left(left.right.get != right.right.get))
            case Ast.Comparator.GreaterThan        => Some(Left(left.right.get >  right.right.get))
            case Ast.Comparator.GreaterThanOrEqual => Some(Left(left.right.get >= right.right.get))
            case Ast.Comparator.LessThan           => Some(Left(left.right.get <  right.right.get))
            case Ast.Comparator.LessThanOrEqual    => Some(Left(left.right.get <= right.right.get))
          }
        } else {
          throw new RuntimeException("cannot compare boolean types to numeric types")
        }

      case None => None
    }
  }

  private def eval(boolOperation: Ast.Expression.BoolOperation): Runtime.EvalOption = {
    (eval(boolOperation.left), eval(boolOperation.right)) match {
      case (Some(left), Some(right)) =>
        boolOperation.op match {
          case Ast.BoolOperator.And => Some(Left(left.left.get && right.left.get))
          case Ast.BoolOperator.Or  => Some(Left(left.left.get || right.left.get))
        }

      case _ => None
    }
  }

  def eval(): Runtime.EvalOption = {
    exp match {
      case bool:    Ast.Expression.Bool          => evalOperand(bool)
      case num:     Ast.Expression.Number        => evalOperand(num)
      case read:    Ast.Expression.VarRead       => evalOperand(read)
      case compute: Ast.Computation              => evalOperand(compute)
      case op:      Ast.Expression.Operation     => eval(op)
      case comp:    Ast.Expression.Comparison    => eval(comp)
      case boolOp:  Ast.Expression.BoolOperation => eval(boolOp)
    }
  }

  override def onStateChange(state: GdaxState, ns: Long): Unit = {
    COMPUTERS.values.foreach(_.listener.onStateChange(state, ns))
  }

  override def onStateSyncStart(ns: Long): Unit = {
    COMPUTERS.values.foreach(_.listener.onStateSyncStart(ns))
  }

  override def onStateSyncEnd(ns: Long): Unit = {
    COMPUTERS.values.foreach(_.listener.onStateSyncEnd(ns))
  }

  def destroy(): Unit = {
    COMPUTERS.clear()
  }

}
