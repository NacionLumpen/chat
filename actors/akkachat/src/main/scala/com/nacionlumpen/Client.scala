package com.nacionlumpen

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Stash}
import akka.io.Tcp
import akka.util.ByteString
import com.nacionlumpen.frames.{Frame, FrameBuffer}
import com.nacionlumpen.model._

class Client(connection: ActorRef, address: InetSocketAddress, channel: ActorRef)
    extends Actor
    with Stash {

  context.watch(connection) // Die with the connection

  val buffer = new FrameBuffer
  channel ! Channel.Register

  private def bufferingMessages(nick: Option[Nick]): Receive = {
    def id = nick.fold(address.toString) { nick =>
      s"$nick@$address"
    }

    {
      case Tcp.Received(data) =>
        buffer
          .append(data)
          .fold(
            error => {
              println(s"closing connection to $id: $error")
              sender ! Tcp.Abort
            },
            frames =>
              frames.foreach { frame =>
                self ! CommandParser.parse(frame.value).valueOr(Client.UnrecognizedCommand.apply)
            })

      case Tcp.PeerClosed =>
        println(s"Connection to $id closed")
        context.stop(self)

      case _: Tcp.ConnectionClosed =>
        println(s"Connection to $id unexpectedly closed")
        context.stop(self)
    }
  }

  private val delayingEverythingElse: Receive = {
    case other => stash()
  }

  private def waitingForCommands(nick: Nick): Receive = bufferingMessages(Some(nick)) orElse {
    case Command.LookupNick =>
      sendMessage(Response.CurrentNick(nick))

    case Command.RenameTo(newNick) =>
      channel ! Channel.Rename(newNick)
      context.become(waitingForChannelResponse(nick))

    case Command.Message(message) =>
      channel ! Channel.SendMsg(message)
      sendMessage(Response.Ok("sent"))

    case Command.LookupNames =>
      channel ! Channel.Names
      context.become(waitingForChannelResponse(nick))

    case Command.Kick(user) =>
      channel ! Channel.Kick(user)
      context.become(waitingForChannelResponse(nick))

    case Command.Quit(message) =>
      channel ! Channel.Unregister(message)
      sendMessage(Response.Ok("bye"))
      context.stop(self)

    case unsupported: Command =>
      sendMessage(Response.UnrecognizedCommand(s"unsupported '${unsupported.toFrame.value}'"))

    case Client.UnrecognizedCommand(message) =>
      sendMessage(Response.UnrecognizedCommand(s"unrecognized command: $message"))

    case Channel.ReceiveMsg(from, message) =>
      sendMessage(Notification.Message(from, message))

    case Channel.Joining(who) =>
      sendMessage(Notification.Joined(who))

    case Channel.Leaving(who, message) =>
      sendMessage(Notification.Left(who, message))

    case Channel.Renamed(from, to) =>
      sendMessage(Notification.Rename(from, to))
  }

  private def waitingForChannelResponse(nick: Nick): Receive =
    ({
      case Channel.Renamed(_, newNick) =>
        println(s"$address's nick is $newNick")
        sendMessage(Response.Ok(s"renamed to $newNick"))
        context.become(waitingForCommands(newNick))

      case Channel.NickInUse =>
        sendMessage(Response.NickInUse("nick is in use"))
        context.become(waitingForCommands(nick))

      case Channel.Kicked(user) =>
        sendMessage(Response.Ok(s"$user was kicked"))
        context.become(waitingForCommands(nick))

      case Channel.UnknownUser(user) =>
        sendMessage(Response.UnknownUser(s"cannot kick unknown user $user"))
        context.become(waitingForCommands(nick))

      case Channel.Names(names) =>
        val (partialRosters, endOfRoster) = Response.Roster.from(names)
        partialRosters.foreach(sendMessage)
        sendMessage(endOfRoster)
        context.become(waitingForCommands(nick))

    }: Receive) orElse bufferingMessages(Some(nick)) orElse delayingEverythingElse

  override def receive =
    ({
      case Channel.Registered(nick) =>
        println(s"$address's initial nick is $nick")
        unstashAll()
        context.become(waitingForCommands(nick))
    }: Receive) orElse bufferingMessages(None) orElse delayingEverythingElse

  private def sendMessage(message: Message): Unit = {
    sendMessage(message.toFrame)
  }

  private def sendMessage(frame: Frame): Unit = {
    connection ! Tcp.Write(ByteString(frame.value + "\n"))
  }

}

object Client {
  val NickCommand = """NICK\s+(\w+)\s*""".r
  case class UnrecognizedCommand(error: String)
}
