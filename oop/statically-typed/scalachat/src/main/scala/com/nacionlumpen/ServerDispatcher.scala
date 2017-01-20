package com.nacionlumpen

import java.net.Socket
import java.util

import com.nacionlumpen.model._

import scala.collection.JavaConversions._

class ServerDispatcher extends Thread {
  val messageQueue = new util.Vector[String]
  val clientsInfo = new util.Vector[ClientInfo]

  def addClient(clientInfo: ClientInfo) = this.synchronized {
    clientsInfo.add(clientInfo)
  }

  def deleteClient(clientInfo: ClientInfo) = this.synchronized {
    clientsInfo.remove(clientInfo)
  }

  def changeNick(clientInfo: ClientInfo, nick: String) = this.synchronized {
    deleteClient(clientInfo)
    addClient(clientInfo.copy(nick = nick))
  }

  def usersConnected = clientsInfo.map(_.nick).mkString(", ")

  def isInUse(name: String) = clientsInfo.map(_.nick).contains(name)

  def getClientInfo(name: String) = clientsInfo.toList.find(c => name.equals(c.nick))

  private def quitClient(clientInfo: ClientInfo) = {
    clientInfo.clientListener.interrupt
    clientInfo.clientSender.interrupt
    deleteClient(clientInfo)
  }

  def dispatchMessage(socket: Socket, message: String) = this.synchronized {
    val clientInfo = clientsInfo.find(client => socket.equals(client.socket)).get
    val nick = """NICK\s*(.*)""".r
    val msg = """MSG\s+(.*)""".r
    val names = """NAMES\s+""".r
    val quit = """QUIT\s+(.*)""".r
    val kick = """KICK\s+(.*)""".r
    message match {
      case nick(name) => name match {
        case empty if name.isEmpty =>
          sendStatus(clientInfo, Created(clientInfo.nick))
        case malformed if User.isMalformed(name) =>
          sendStatus(clientInfo, MovedPermanently("Malformed nick"))
        case inUse if isInUse(name) =>
          sendStatus(clientInfo, Found("Nick name in use"))
        case name if changeNick(clientInfo, name) =>
          sendStatus(clientInfo, Ok("Nick named accepted"))
          messageQueue.add(s"RENAME ${clientInfo.nick} $name")
        case _ => NotImplemented("Command not found")
      }
      case names(_) =>
        sendStatus(clientInfo, Accepted(usersConnected))
      case quit(partingMessage) =>
        sendStatus(clientInfo, Ok(s"Command acknowledged"))
        messageQueue.add(s"$partingMessage")
        quitClient(clientInfo)
      case kick(nick) => getClientInfo(nick) match {
          case Some(kicked) =>
            sendStatus(kicked, Ok("Bon voyage"))
            quitClient(clientInfo)
          case None => sendStatus(clientInfo, SeeOther("Unknown user"))
        }
      case msg(message) =>
          if (messageQueue.add(s"MSG ${clientInfo.nick}: $message"))
            sendStatus(clientInfo, Ok("Message accepted"))
      case _ => sendStatus(clientInfo, NotImplemented("Command not found"))
    }
    notify()
  }

  @throws(classOf[InterruptedException])
  private def getNextMessageFromQueue(): String = this.synchronized {
    while(messageQueue.isEmpty)
      wait()
    val message = messageQueue.get(0)
    messageQueue.removeElementAt(0)
    message
  }

  private def sendStatus(clientInfo: ClientInfo, status: Status) = {
    clientInfo.clientSender.sendMessage(status.toString)
  }

  private def sendMessageToAllClients(message: String) = this.synchronized {
    clientsInfo.toList.foreach(_.clientSender.sendMessage(message))
  }

  override def run(): Unit = {
    while(true) {
      sendMessageToAllClients(getNextMessageFromQueue())
    }
  }
}
