package com.nacionlumpen

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Stash}
import akka.io.Tcp
import akka.util.ByteString
import com.nacionlumpen.model._
import scalaz._
import Scalaz._

class Client(connection: ActorRef, address: InetSocketAddress, channel: ActorRef)
    extends Actor
    with Stash {

  context.watch(connection) // Die with the connection

  val buffer = new MessageBuffer
  channel ! Channel.Register

  private def bufferingMessages(nick: Option[Nick]): Receive = {
    def id = nick.fold(address.toString) { nick =>
      s"$nick@$address"
    }

    {
      case Tcp.Received(data) =>
        buffer
          .append(data)
          .flatMap(_.traverseU(CommandParser.parse))
          .fold(error => {
            println(s"closing connection to $id: $error")
            sender ! Tcp.Abort
          }, commands => commands.foreach(self ! _))

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
      sendMessage(Response.Ok(""))

    case Command.LookupNames =>
      channel ! Channel.Names
      context.become(waitingForChannelResponse(nick))

    case Command.Kick(user) =>
      channel ! Channel.Kick(user)
      context.become(waitingForChannelResponse(nick))

    case unsupported: Command =>
      sendMessage(Response.UnrecognizedCommand(s"$unsupported is unsupported"))

    case Channel.ReceiveMsg(from, message) =>
      sendMessage(Notification.Message(from, message))
  }

  private def waitingForChannelResponse(nick: Nick): Receive =
    ({
      case Channel.Renamed(newNick) =>
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
        sendMessage(Response.Roster(names)) // TODO: split long list of names
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
    connection ! Tcp.Write(ByteString(message + "\n")) // TODO: respect message size limit
  }

}

object Client {
  val NickCommand = """NICK\s+(\w+)\s*""".r
}
