package com.nacionlumpen.frames

import com.nacionlumpen.model.ProtocolConstants

case class Frame(value: String) {
  require(value.length <= ProtocolConstants.MaxFrameSize)
}

object Frame {
  def truncating(value: String): Frame = Frame(value.take(ProtocolConstants.MaxFrameSize))
}
