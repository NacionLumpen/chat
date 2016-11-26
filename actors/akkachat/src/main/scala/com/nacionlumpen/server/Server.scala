package com.nacionlumpen.server

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.{IO, Tcp}

class Server(port: Int) extends Actor {
  import context.system

  IO(Tcp) ! Tcp.Bind(self, new InetSocketAddress("localhost", port))
  val channel = context.actorOf(Props[Channel], "chat")

  override def receive = {
    case Tcp.CommandFailed(_: Tcp.Bind) =>
      println("Cannot listen")
      context.stop(self)
      context.system.terminate()

    case Tcp.Bound(address) =>
      println(s"Listening on $address")

    case Tcp.Connected(address, _) =>
      println(s"Incoming connection from $address")
      val connection = sender
      val clientProps = Props(new Client(connection, address, channel))
      connection ! Tcp.Register(context.actorOf(clientProps))
  }
}

object Server {
  def run(port: Int): Unit = {
    ActorSystem("chat").actorOf(Props(new Server(port)))
  }
}
