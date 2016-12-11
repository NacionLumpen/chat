package com.nacionlumpen

import java.io._
import java.net.{ServerSocket, Socket}

import com.nacionlumpen.model.{NotImplemented, Ok}

object EchoServer extends App {
  val port = 4444
  val serverSocket = new ServerSocket(port)
  println(s"Started server on port $port")
  while(true) {
    val clientSocket = serverSocket.accept()
    println("Accepted connection from client")
    new Thread(new Client(clientSocket)).start()
  }
}

class Client(socket: Socket) extends Runnable {
  override def run(): Unit = try {
    val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
    val out = new PrintStream(socket.getOutputStream)

    var line = ""
    while ((line = in.readLine()) != null) {
      val nick = """NICK\s+(.*)""".r
      line match {
        case nick(name) => out.println(Ok("Nick named accepted"))
        case _ => out.println(NotImplemented("Command not found"))
      }
    }

    println("Closing connection with client")
    out.close()
    in.close()
  } finally socket.close()
}
