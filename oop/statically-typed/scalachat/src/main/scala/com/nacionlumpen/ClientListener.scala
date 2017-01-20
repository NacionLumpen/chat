package com.nacionlumpen

import java.io.{BufferedReader, InputStreamReader}
import java.net.Socket

class ClientListener(socket: Socket, serverDispatcher: ServerDispatcher) extends Thread {
  val in = new BufferedReader(new InputStreamReader(socket.getInputStream))

  override def run(): Unit = {
    while (!isInterrupted) {
      val message = in.readLine()
      serverDispatcher.dispatchMessage(socket, message)
    }
  }
}
