package com.nacionlumpen

import scalaz.\/
import scalaz.syntax.either._

import akka.util.ByteString
import com.nacionlumpen.model.ProtocolConstants

// TODO: introduce a Frame class wrapping strings up to the max frame size
class MessageBuffer(maxSize: Int = ProtocolConstants.MaxFrameSize) {
  import MessageBuffer._

  private var buffer = ByteString.empty

  def append(data: ByteString): String \/ Vector[String] = {
    buffer ++= data

    if (messageSizeOverrun) s"too long message (${buffer.size})".left
    else {
      var messages = Vector.empty[String]
      while (buffer.contains(Delimiter)) {
        val (command, remaining) = buffer.span(_ != Delimiter)
        buffer = remaining.tail
        messages :+= command.utf8String
      }
      messages.right
    }
  }

  private def messageSizeOverrun: Boolean = {
    val index = buffer.indexOf(Delimiter)
    (index < 0 && buffer.size > maxSize) || index > maxSize
  }
}

object MessageBuffer {
  val Delimiter = '\n'.toByte
}
