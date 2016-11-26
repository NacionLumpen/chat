package com.nacionlumpen

import scalaz.DRight

import com.nacionlumpen.Arbitraries._
import com.nacionlumpen.model.Command
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class CommandParserTest extends FlatSpec with Matchers with PropertyChecks {

  "Any command" should "have round trip parsing" in {
    forAll { (command: Command) =>
      CommandParser.parse(command.toFrame.value) shouldBe DRight(command)
    }
  }
}
