package com.nacionlumpen.model

import scalaz.NonEmptyList

import com.nacionlumpen.frames.Frame
import scalaz.syntax.std.list._

sealed trait Message extends Product with Serializable {
  def toFrame: Frame
}

sealed trait Command extends Message

object Command {
  case object LookupNick extends Command {
    override def toFrame = Frame("NICK")
  }
  case class RenameTo(nick: Nick) extends Command {
    override def toFrame = Frame(s"NICK $nick")
  }
  case class Message(message: String) extends Command {
    override def toFrame = Frame.truncating(s"MSG $message".trim)
  }
  case object LookupNames extends Command {
    override def toFrame = Frame("NAMES")
  }
  case class Kick(nick: Nick) extends Command {
    override def toFrame = Frame(s"KICK $nick")
  }
  case class Quit(message: String) extends Command {
    override def toFrame = Frame.truncating(s"QUIT $message".trim)
  }
}

sealed trait ServerMessage extends Message

sealed trait Response extends ServerMessage {
  val statusCode: Int
  val status: String
  override def toFrame = Frame.truncating(s"$statusCode $status".trim)
}

object Response {
  case class Ok(override val status: String) extends Response {
    override val statusCode = 200
  }

  case class CurrentNick(nick: Nick) extends Response {
    override val statusCode = 201
    override val status = nick.value
  }

  case class PartialRoster(nicks: NonEmptyList[Nick]) extends Response {
    override val statusCode = 203
    override val status = nicks.stream.map(_.value).mkString(" ")
    override def toFrame = Frame(s"$statusCode $status")
  }

  case class Roster(nicks: List[Nick]) extends Response {
    override val statusCode = 202
    override val status = nicks.map(_.value).mkString(" ")
    override def toFrame = Frame(s"$statusCode $status")
  }

  object Roster {
    def from(nicks: Seq[Nick]): (List[PartialRoster], Roster) = {
      val nickGroups = nicks.foldLeft(List.empty[List[Nick]]) {
        case (Nil, nick) => List(List(nick))
        case (group :: groups, nick) if fitsInAMessage(nick :: group) =>
          (nick :: group) :: groups
        case (groups, nick) => List(nick) :: groups
      }.reverse
      if (nickGroups.isEmpty) (List.empty, Roster(List.empty))
      else
        nickGroups.init.flatMap(_.toNel).map(PartialRoster.apply) -> Roster(nickGroups.last)
    }

    private def fitsInAMessage(nicks: List[Nick]): Boolean =
      3 + nicks.map(_.value.length + 1).sum <= ProtocolConstants.MaxFrameSize
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
    override def toFrame = Frame.truncating(s"MSG $author $message")
  }
  case class Joined(user: Nick) extends Notification {
    override def toFrame = Frame(s"JOINED $user")
  }
  case class Rename(from: Nick, to: Nick) extends Notification {
    override def toFrame = Frame(s"RENAME $from $to")
  }
  case class Left(user: Nick, message: String) extends Notification {
    override def toFrame = Frame.truncating(s"LEFT $user $message")
  }
}
