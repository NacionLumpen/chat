package com.nacionlumpen

import com.nacionlumpen.model.ProtocolConstants
import com.nacionlumpen.server.Server

object Main extends App {
  args match {
    case Array("listen") => Server.run(ProtocolConstants.Port)
    case Array("listen", port) => Server.run(port.toInt)
    case Array("connect", address) => println("TODO")
    case _ =>
      println(s"""Usage: $$COMMAND listen [<port>]
                 |       $$COMMAND connect <address>
                 |
                 |Where $$COMMAND is either `sbt run` or `java -jar akkachat.jar`.
                 |Default port is ${ProtocolConstants.Port}
        """.stripMargin)
      System.exit(-1)
  }

}
