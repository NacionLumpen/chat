package com.nacionlumpen.model

sealed trait Message extends Product with Serializable

sealed trait Command extends Message

object Command {
  case object LookupNick extends Command {
    override def toString = "NICK"
  }
  case class RenameTo(nick: Nick) extends Command {
    override def toString = s"NICK $nick"
  }
  case class Message(message: String) extends Command {
    override def toString = s"MSG $message"
  }
  case object LookupNames extends Command {
    override def toString = "NAMES"
  }
  case class Kick(nick: Nick) extends Command {
    override def toString = s"KICK $nick"
  }
}

sealed trait ServerMessage extends Message

sealed trait Response extends ServerMessage {
  val statusCode: Int
  val status: String
  override def toString = s"$statusCode $status".trim
}

object Response {
  case class Ok(override val status: String) extends Response {
    override val statusCode = 200
  }

  case class CurrentNick(nick: Nick) extends Response {
    override val statusCode = 201
    override val status = nick.value
  }

  case class Roster(nicks: Vector[Nick]) extends Response {
    override val statusCode = 202
    override val status = nicks.map(_.value).mkString(" ")
  }

  case class PartialRoster(nicks: Vector[Nick]) extends Response {
    override val statusCode = 203
    override val status = nicks.map(_.value).mkString(" ")
  }

  case class MalformedNick(override val status: String) extends Response {
    override val statusCode = 301
  }

  case class NickInUse(override val status: String) extends Response {
    override val statusCode = 302
  }

  case class UnknownUser(override val status: String) extends Response {
    override val statusCode = 303
  }

  case class UnrecognizedCommand(override val status: String) extends Response {
    override val statusCode = 400
  }
}

sealed trait Notification extends ServerMessage

object Notification {
  case class Message(author: Nick, message: String) extends Notification {
    override def toString = s"MSG $author $message"
  }
  case class Joined(user: Nick) extends Notification {
    override def toString = s"JOINED $user"
  }
  case class Rename(from: Nick, to: Nick) extends Notification {
    override def toString = s"RENAME $from $to"
  }
  case class Left(user: Nick) extends Notification {
    override def toString = s"LEFT $user"
  }
}
