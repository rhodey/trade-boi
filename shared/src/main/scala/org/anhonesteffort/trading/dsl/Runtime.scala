package org.anhonesteffort.trading.dsl

import org.anhonesteffort.trading.dsl.Ast.{Expression, Statement}
import org.anhonesteffort.trading.state.{GdaxState, StateListener}

import scala.collection.mutable

object Runtime {

  type EvalResult = Either[Boolean, Double]
  type EvalOption = Option[EvalResult]

  class DslContext extends StateListener {
    private val VARIABLES = mutable.HashMap[String, Evaluator]()

    def readVar(name: String): EvalOption = {
      VARIABLES.get(name) match {
        case Some(eval) => eval.eval()
        case None       => throw new RuntimeException(s"variable with name '$name' is undefined")
      }
    }

    private def destroyVar(name: String): Unit = {
      VARIABLES.remove(name) match {
        case Some(eval) => eval.destroy()
        case None => throw new RuntimeException(s"variable with name '$name' is undefined")
      }
    }

    private def createVar(name: String, exp: Expression): EvalOption = {
      if (VARIABLES.contains(name)) {
        destroyVar(name)
      }

      VARIABLES.put(name, Evaluator(this, exp))
      readVar(name)
    }

    private def evaluate(exp: Expression): EvalOption = {
      val eval   = Evaluator(this, exp)
      val option = eval.eval()
      eval.destroy()
      option
    }

    def eval(statement: Statement): Either[EvalOption, Unit] = {
      statement match {
        case stmt: Statement.CreateVar  => Left(createVar(stmt.name, stmt.exp))
        case stmt: Statement.DestroyVar => Right(destroyVar(stmt.name))
        case stmt: Statement.Evaluate   => Left(evaluate(stmt.exp))
        case       Statement.NoOp       => Right()
      }
    }

    def eval(statement: String): Unit = {
      val parsed = Parsers.STATEMENT.parse(statement)

      parsed.fold(
        (_, _, _) => println(s"error: ${parsed.toString}"),
        (stmt, _) => eval(stmt) match {
          case Left(option) => option match {
            case None                => println(option)
            case Some(Left(bool))    => println(bool)
            case Some(Right(double)) => println(f"$double%1.4f")
          }
          case _ => Unit
        }
      )
    }

    override def onStateChange(state: GdaxState, ns: Long): Unit = {
      VARIABLES.values.foreach(_.onStateChange(state, ns))
    }

    override def onStateSyncStart(ns: Long): Unit = {
      VARIABLES.values.foreach(_.onStateSyncStart(ns))
    }

    override def onStateSyncEnd(ns: Long): Unit = {
      VARIABLES.values.foreach(_.onStateSyncEnd(ns))
    }
  }

}
