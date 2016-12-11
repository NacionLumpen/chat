package com.nacionlumpen.model

sealed trait Status {
  val code: Int
  val message: String
  override def toString: String = s"$code $message"
}

case class Ok(message: String) extends Status {
  override val code: Int = 200
}
case class NotImplemented(message: String) extends Status {
  override val code: Int = 501
}