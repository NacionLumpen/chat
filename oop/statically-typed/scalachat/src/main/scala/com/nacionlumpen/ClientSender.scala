package com.nacionlumpen

import java.io.PrintStream
import java.net.Socket
import java.util

class ClientSender(socket: Socket, serverDispatcher: ServerDispatcher) extends Thread {
  val messageQueue = new util.Vector[String]
  val out = new PrintStream(socket.getOutputStream)

  def sendMessage(message: String) = this.synchronized {
    messageQueue.add(message)
    notify()
  }

  @throws(classOf[InterruptedException])
  private def getNextMessageFromQueue = this.synchronized {
    while(messageQueue.isEmpty) wait()
    val message = messageQueue.get(0)
    messageQueue.removeElementAt(0)
    message
  }

  private def sendMessageToClient(message: String) = {
    out.println(message)
    out.flush()
  }

  override def run(): Unit = {
    while(!isInterrupted) {
      sendMessageToClient(getNextMessageFromQueue)
    }
  }
}
