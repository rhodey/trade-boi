package org.anhonesteffort.trading.dsl

import fastparse.all._

object Parsers {

  val BOOLEAN: Parser[Boolean] = P("true" | "false").!.map(_.toBoolean)
  val DOUBLE:  Parser[Double]  = P(CharIn('0' to '9').rep(1) ~ ("." ~ CharIn('0' to '9').rep(1)).?).!.map(_.toDouble)
  val VAR:     Parser[String]  = P("$" ~ (CharIn('a' to 'z') | CharIn('A' to 'Z') | CharIn('0' to '9')).rep(1).!)

  val OPERATOR: Parser[Ast.Operator] = P("+" | "-" | "*" | "/").!.map({
    case "+" => Ast.Operator.Add
    case "-" => Ast.Operator.Subtract
    case "*" => Ast.Operator.Multiply
    case "/" => Ast.Operator.Divide
  })

  val COMPARATOR: Parser[Ast.Comparator] = P(("<" ~ "=".?) | (">" ~ "=".?)).!.map({
    case "<"  => Ast.Comparator.LessThan
    case "<=" => Ast.Comparator.LessThanOrEqual
    case ">"  => Ast.Comparator.GreaterThan
    case ">=" => Ast.Comparator.GreaterThanOrEqual
  })

  val BOOL_COMPARATOR: Parser[Ast.BoolComparator] = P("==" | "!=").!.map({
    case "==" => Ast.BoolComparator.Equal
    case "!=" => Ast.BoolComparator.NotEqual
  })

  val BOOL_OPERATOR: Parser[Ast.BoolOperator] = P("&&" | "||").!.map({
    case "&&" => Ast.BoolOperator.And
    case "||" => Ast.BoolOperator.Or
  })

  val SIDE: Parser[Ast.Side] = P("bid" | "ask").!.map({
    case "bid" => Ast.Side.Bid
    case "ask" => Ast.Side.Ask
  })

  val PERIOD_UNIT: Parser[Ast.PeriodUnit] = P("ms" | "s" | "m").!.map({
    case "ms" => Ast.PeriodUnit.Milliseconds
    case "s"  => Ast.PeriodUnit.Seconds
    case "m"  => Ast.PeriodUnit.Minutes
  })

  val PERIOD: Parser[Ast.Period] = P(DOUBLE ~ PERIOD_UNIT).map(tup => Ast.Period(tup._1, tup._2))

  val SPREAD: Parser[Ast.Computation] = P("spread()").map(_ => Ast.Computation.Spread)
  val TAKE:   Parser[Ast.Computation] = P("take(" ~ SIDE ~ ")").map(side => Ast.Computation.TakeVolume(side))
  val SUM:    Parser[Ast.Computation] = P("sum(" ~ (SPREAD | TAKE) ~ ", " ~ PERIOD ~ ")").map(tup => Ast.Computation.Sum(tup._1, tup._2))

  val BOOL:        Parser[Ast.Expression.Bool]    = P(BOOLEAN).map(Ast.Expression.Bool)
  val NUMBER:      Parser[Ast.Expression.Number]  = P(DOUBLE).map(Ast.Expression.Number)
  val VAR_READ:    Parser[Ast.Expression.VarRead] = P(VAR).map(Ast.Expression.VarRead)
  val COMPUTATION: Parser[Ast.Computation]        = P(SPREAD | TAKE | SUM)

  val OPERATION: Parser[Ast.Expression.Operation] = P(
    (NUMBER | VAR_READ | COMPUTATION) ~ " " ~ OPERATOR ~ " " ~ (NUMBER | VAR_READ | COMPUTATION)
  ).map(tup => Ast.Expression.Operation(tup._1, tup._2, tup._3))

  val COMPARISON: Parser[Ast.Expression.Comparison] = P(
    ((NUMBER | VAR_READ | COMPUTATION) ~ " " ~ (COMPARATOR | BOOL_COMPARATOR) ~ " " ~ (NUMBER | VAR_READ | COMPUTATION)) |
      ((BOOL | VAR_READ) ~ " " ~ BOOL_COMPARATOR ~ " " ~ (BOOL | VAR_READ))
  ).map(tup => Ast.Expression.Comparison(tup._1, tup._2, tup._3))

  val BOOL_OPERATION: Parser[Ast.Expression.BoolOperation] = P(
    COMPARISON ~ " " ~ BOOL_OPERATOR ~ " " ~ COMPARISON
  ).map(tup => Ast.Expression.BoolOperation(tup._1, tup._2, tup._3))

  val EXPRESSION: Parser[Ast.Expression] = P(
    ((BOOL | NUMBER | VAR_READ | COMPUTATION) ~ End) |
      ((OPERATION | COMPARISON) ~ End) |
      (BOOL_OPERATION ~ End)
  )

  val CREATE_VAR:  Parser[Ast.Statement] = P(VAR ~ " = " ~ EXPRESSION).map(tup => Ast.Statement.CreateVar(tup._1, tup._2))
  val DESTROY_VAR: Parser[Ast.Statement] = P("!" ~ VAR ~ End).map(Ast.Statement.DestroyVar)
  val EVALUATE:    Parser[Ast.Statement] = P(EXPRESSION).map(Ast.Statement.Evaluate)
  val NO_OP:       Parser[Ast.Statement] = P(End).map(_ => Ast.Statement.NoOp)
  val EXIT:        Parser[Ast.Statement] = P("exit()" ~ End).map(_ => Ast.Statement.Exit)
  val STATEMENT:   Parser[Ast.Statement] = P(CREATE_VAR | DESTROY_VAR | EVALUATE | NO_OP | EXIT)

}
