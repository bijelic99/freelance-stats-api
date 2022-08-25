package model

case class AccessForbiddenException(message: String) extends Exception(message)
