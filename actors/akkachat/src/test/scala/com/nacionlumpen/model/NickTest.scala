package com.nacionlumpen.model

import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class NickTest extends FlatSpec with Matchers with PropertyChecks {

  val validChars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9') ++ Seq('_', '-')

  val validNicks: Gen[String] = for {
    size <- Gen.chooseNum(1, 10)
    chars <- Gen.listOfN(size, Gen.oneOf(validChars))
  } yield chars.mkString

  "A nick" should "have no more than 10 characters" in {
    noException shouldBe thrownBy { Nick.of("anon") }
    an[IllegalArgumentException] shouldBe thrownBy { Nick.of("anon1234567890") }
  }

  it should "have at least a character" in {
    an[IllegalArgumentException] shouldBe thrownBy { Nick.of("") }
  }

  it should "contain only valid characters" in {
    forAll(validNicks) { text =>
      Nick.parse(text) should not be empty
    }

    forAll { (text: String) =>
      whenever(text.exists(c => !validChars.contains(c))) {
        Nick.parse(text) shouldBe empty
      }
    }
  }
}
