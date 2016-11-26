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
      users.keySet.foreach(_ ! Channel.Joining(nick))
      users += (sender -> nick)
      context.watch(sender)
      sender ! Channel.Registered(nick)

    case Channel.Unregister(message) =>
      users.get(sender).foreach { nick =>
        users -= sender
        users.keySet.foreach(_ ! Channel.Leaving(nick, message))
      }

    case Channel.Rename(nick) if users.values.exists(_ == nick) => sender ! Channel.NickInUse

    case Channel.Rename(newNick) =>
      users.get(sender).foreach { oldNick =>
        users += (sender -> newNick)
        users.keys.foreach(_ ! Channel.Renamed(oldNick, newNick))
      }

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

    case Terminated(user) =>
      users.get(user).foreach { nick =>
        users -= user
        users.keys.foreach(_ ! Channel.Leaving(nick, ""))
      }
  }

}

object Channel {
  case object Register
  case class Registered(as: Nick)
  case class Joining(nick: Nick)

  case class Unregister(message: String)
  case class Leaving(nick: Nick, message: String)

  case class Rename(to: Nick)
  case object NickInUse
  case class Renamed(from: Nick, to: Nick)

  case class SendMsg(message: String)
  case class ReceiveMsg(nick: Nick, message: String)

  case object LookupNames
  case class Names(names: Vector[Nick])

  case class Kick(user: Nick)
  case class Kicked(user: Nick)
  case class UnknownUser(user: Nick)
}
