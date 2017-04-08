package org.anhonesteffort.trading.dsl

object Ast {

  sealed trait Operator
  case object Operator {
    case object Add      extends Operator
    case object Subtract extends Operator
    case object Multiply extends Operator
    case object Divide   extends Operator
  }

  sealed trait Comparator
  object Comparator {
    case object LessThan           extends Comparator
    case object LessThanOrEqual    extends Comparator
    case object GreaterThan        extends Comparator
    case object GreaterThanOrEqual extends Comparator
  }

  sealed trait BoolComparator extends Comparator
  object BoolComparator {
    case object Equal    extends BoolComparator
    case object NotEqual extends BoolComparator
  }

  sealed trait BoolOperator
  object BoolOperator {
    case object And extends BoolOperator
    case object Or  extends BoolOperator
  }

  sealed trait Expression
  object Expression {
    case class Bool(value: Boolean) extends Expression
    case class Number(value: Double) extends Expression
    case class VarRead(name: String) extends Expression
    case class Operation(left: Expression, op: Operator, right: Expression) extends Expression
    case class Comparison(left: Expression, comp: Comparator, right: Expression) extends Expression
    case class BoolOperation(left: Comparison, op: BoolOperator, right: Comparison) extends Expression
  }

  sealed trait Side
  object Side {
    case object Bid extends Side
    case object Ask extends Side
  }

  sealed trait PeriodUnit
  object PeriodUnit {
    case object Milliseconds extends PeriodUnit { override def toString = "ms" }
    case object Seconds      extends PeriodUnit { override def toString = "s"  }
    case object Minutes      extends PeriodUnit { override def toString = "m"  }
  }

  case class Period(value: Double, unit: PeriodUnit) {
    def milliseconds: Long = {
      unit match {
        case PeriodUnit.Milliseconds => value.toLong
        case PeriodUnit.Seconds      => (value * 1000).toLong
        case PeriodUnit.Minutes      => (value * 1000 * 60).toLong
      }
    }

    override def toString: String = {
      s"$value$unit"
    }
  }

  sealed trait Computation extends Expression
  object Computation {
    case object Spread extends Computation
    case class  TakeVolume(side: Side) extends Computation
    case class  Sum(child: Computation, period: Period) extends Computation
  }

  sealed trait Statement
  object Statement {
    case class  CreateVar(name: String, exp: Expression) extends Statement
    case class  DestroyVar(name: String) extends Statement
    case class  Evaluate(exp: Expression) extends Statement
    case object NoOp extends Statement
  }

}
