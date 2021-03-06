package com.nacionlumpen.frames

import scalaz.\/
import scalaz.syntax.either._

import akka.util.ByteString
import com.nacionlumpen.model.ProtocolConstants

class FrameBuffer(maxSize: Int = ProtocolConstants.MaxFrameSize) {
  import FrameBuffer._

  private var buffer = ByteString.empty

  def append(data: ByteString): String \/ Vector[Frame] = {
    buffer ++= data

    if (messageSizeOverrun) s"too long message (${buffer.size})".left
    else {
      var messages = Vector.empty[Frame]
      while (buffer.contains(Delimiter)) {
        val (command, remaining) = buffer.span(_ != Delimiter)
        buffer = remaining.tail
        messages :+= Frame(command.utf8String)
      }
      messages.right
    }
  }

  private def messageSizeOverrun: Boolean = {
    val index = buffer.indexOf(Delimiter)
    (index < 0 && buffer.size > maxSize) || index > maxSize
  }
}

object FrameBuffer {
  val Delimiter = '\n'.toByte
}
