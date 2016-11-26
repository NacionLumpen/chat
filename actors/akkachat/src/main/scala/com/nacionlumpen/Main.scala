package com.nacionlumpen

import com.nacionlumpen.model.ProtocolConstants
import com.nacionlumpen.server.Server

object Main extends App {
  Server.run(ProtocolConstants.Port)
}
