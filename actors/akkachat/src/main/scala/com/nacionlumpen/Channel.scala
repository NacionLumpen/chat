package com.nacionlumpen

import akka.actor._
import com.nacionlumpen.model.Nick

class Channel extends Actor {
  var nextId = 1
  var users = Map.empty[ActorRef, Nick]

  override def receive = {
    case Channel.Register =>
      val nick = Nick.of(s"anon$nextId")
      nextId += 1
      users += (sender -> nick)
      // TODO: subscribe to client death
      sender ! Channel.Registered(nick)

    case Channel.Rename(nick) if users.values.exists(_ == nick) => sender ! Channel.NickInUse

    case Channel.Rename(nick) =>
      users += (sender -> nick)
      sender ! Channel.Renamed(nick)

    case Channel.SendMsg(payload) =>
      for (senderNick <- users.get(sender);
           message = Channel.ReceiveMsg(senderNick, payload);
           receiver <- users.keys if receiver != sender) {
        receiver ! message
      }

    case Channel.Names =>
      sender ! Channel.Names(users.values.toVector.sortBy(_.value))

    case Channel.Kick(nick) =>
      val maybeUser = users.collectFirst {
        case (user, `nick`) => user
      }
      maybeUser.fold(sender ! Channel.UnknownUser(nick)) { user =>
        users -= user
        user ! PoisonPill
        sender ! Channel.Kicked(nick)
      }

    case Channel.Kick(nick) => sender ! Channel.UnknownUser(nick)
  }

}

object Channel {
  case object Register
  case class Registered(as: Nick)

  case class Rename(to: Nick)
  case object NickInUse
  case class Renamed(to: Nick)

  case class SendMsg(message: String)
  case class ReceiveMsg(nick: Nick, message: String)

  case object LookupNames
  case class Names(names: Vector[Nick])

  case class Kick(user: Nick)
  case class Kicked(user: Nick)
  case class UnknownUser(user: Nick)
}
