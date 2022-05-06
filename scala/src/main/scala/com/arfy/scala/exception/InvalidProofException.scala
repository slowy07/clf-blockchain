package com.arfy.scala.exception

final class InvalidProofException(val hash: String, val proof: Long, val Message: String = "", val cause: Throwable = None.orNull) extends Exception(message, cause) 
