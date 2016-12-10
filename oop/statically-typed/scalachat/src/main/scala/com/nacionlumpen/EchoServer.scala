package com.nacionlumpen

import java.io._
import java.net.ServerSocket

object EchoServer extends App {
  val port = 4444
  val serverSocket = new ServerSocket(port)
  println(s"Started server on port $port")
  while(true) {
    val clientSocket = serverSocket.accept()
    println("Accepted connection from client")

    val in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream))
    val out = new PrintStream(clientSocket.getOutputStream)

    var line = ""
    while ((line = in.readLine()) != null) {
      println(line)
      out.println(line)
    }

    println("Closing connection with client")
    out.close()
    in.close()
    clientSocket.close()
  }
}
