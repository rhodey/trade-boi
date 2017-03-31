package org.anhonesteffort.trading.dsl

import org.anhonesteffort.trading.dsl.Ast.{Expression, Statement}

import scala.collection.mutable

object Runtime {

  type EvalResult = Either[Boolean, Double]
  type EvalOption = Option[EvalResult]

  class Context {

    private val VARIABLES = mutable.HashMap[String, Evaluator]()

    def readVar(name: String): EvalOption = {
      VARIABLES.get(name) match {
        case Some(eval) => eval.eval()
        case None => throw new RuntimeException(s"variable with name '$name' is undefined")
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

    private def exit(): String = {
      "exit()"
    }

    def eval(statement: Statement): Any = {
      statement match {
        case stmt: Statement.CreateVar  => createVar(stmt.name, stmt.exp)
        case stmt: Statement.DestroyVar => destroyVar(stmt.name)
        case stmt: Statement.Evaluate   => evaluate(stmt.exp)
        case       Statement.NoOp       => None
        case       Statement.Exit       => exit()
      }
    }

  }
}
