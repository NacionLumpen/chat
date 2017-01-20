package com.nacionlumpen.model

sealed trait Status {
  val code: Int
  val message: String
  override def toString: String = s"$code $message"
}

case class Ok(message: String) extends Status {
  override val code: Int = 200
}
case class Created(message: String) extends Status {
  override val code: Int = 201
}
case class Accepted(message: String) extends Status {
  override val code: Int = 202
}
case class MovedPermanently(message: String) extends Status {
  override val code: Int = 301
}
case class Found(message: String) extends Status {
  override val code: Int = 302
}
case class SeeOther(message: String) extends Status {
  override val code: Int = 303
}
case class NotImplemented(message: String) extends Status {
  override val code: Int = 501
}