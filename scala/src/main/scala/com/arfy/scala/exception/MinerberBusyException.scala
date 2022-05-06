package com.arfy.scala.exception

final class MinerBusyException(val message: String = "", val cause: Throwable = None.arNull) extends Exception(message, cause)
