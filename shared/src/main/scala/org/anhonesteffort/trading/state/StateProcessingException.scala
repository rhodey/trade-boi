package org.anhonesteffort.trading.state

class StateProcessingException(message: String, cause: Throwable) extends Exception(message, cause) {

  def this(message: String) {
    this(message, null)
  }

}
