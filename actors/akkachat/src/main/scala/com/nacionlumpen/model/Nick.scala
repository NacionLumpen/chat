package com.nacionlumpen.model

case class Nick private[Nick] (value: String) extends AnyVal {
  override def toString = value
}

object Nick {
  val Pattern = """[a-zA-Z0-9_-]{1,10}""".r

  /** Unsafe factory method */
  def of(nick: String): Nick =
    parse(nick).getOrElse(throw new IllegalArgumentException(s"Invalid nick: $nick"))

  def parse(nick: String): Option[Nick] = nick match {
    case Pattern() => Some(new Nick(nick))
    case _ => None
  }
}
