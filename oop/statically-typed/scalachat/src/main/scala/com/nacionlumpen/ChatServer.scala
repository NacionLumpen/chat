package com.nacionlumpen

import java.net.{ServerSocket, Socket}

object ChatServer extends App {
  val port = 4444
  val serverSocket = new ServerSocket(port)
  println(s"ChatServer started on port $port")

  val serverDispatcher = new ServerDispatcher
  serverDispatcher.start()

  while (true) {
    val socket = serverSocket.accept()
    println("Accepted connection from client")
    val clientListener = new ClientListener(socket, serverDispatcher)
    val clientSender = new ClientSender(socket, serverDispatcher)
    clientListener.start()
    clientSender.start()
    serverDispatcher.addClient(ClientInfo(socket, clientListener, clientSender, "unknown"))
  }
}

case class ClientInfo(socket: Socket, clientListener: ClientListener, clientSender: ClientSender, nick: String)
