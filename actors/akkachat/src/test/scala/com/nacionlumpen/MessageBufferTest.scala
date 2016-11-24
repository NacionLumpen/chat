package com.nacionlumpen

import scalaz.DRight

import akka.util.ByteString
import org.scalatest.{FlatSpec, Matchers}

class MessageBufferTest extends FlatSpec with Matchers {

  "A message buffer" should "detect no message when input data is empty" in {
    val buffer = new MessageBuffer(maxSize = 10)
    buffer.append(ByteString("")) shouldBe DRight(Seq.empty)
  }

  it should "report failure when a message is too large" in {
    val buffer = new MessageBuffer(maxSize = 10)
    buffer.append(ByteString("hello"))
    buffer.append(ByteString("this message is too large")) shouldBe 'left
  }

  it should "return a complete message" in {
    val buffer = new MessageBuffer(maxSize = 10)
    buffer.append(ByteString("hello\n")) shouldBe DRight(Seq("hello"))
  }

  it should "return multiple messages" in {
    val buffer = new MessageBuffer(maxSize = 10)
    buffer.append(ByteString("hel")) shouldBe DRight(Seq.empty)
    buffer.append(ByteString("lo\nguys\n")) shouldBe DRight(Seq("hello", "guys"))
  }
}
