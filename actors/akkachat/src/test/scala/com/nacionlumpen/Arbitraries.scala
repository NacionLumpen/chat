package com.nacionlumpen

import com.nacionlumpen.model.{Command, Nick}
import org.scalacheck.Arbitrary._
import org.scalacheck.{Arbitrary, Gen}

object Arbitraries {

  implicit val arbNick = Arbitrary[Nick](for {
    size <- Gen.chooseNum(1, 10)
    chars <- Gen
      .listOfN(size, Gen.oneOf(Gen.choose('a', 'z'), Gen.choose('A', 'Z'), Gen.oneOf('_', '-')))
  } yield Nick.of(chars.mkString))

  val genPayload = Gen.listOf(Gen.oneOf(Gen.alphaNumChar, Gen.const(' '))).map(_.mkString.trim)

  implicit val arbRenameToCommand =
    Arbitrary[Command.RenameTo](arbitrary[Nick].map(Command.RenameTo.apply))

  implicit val arbMessageCommand =
    Arbitrary[Command.Message](genPayload.map(Command.Message.apply))

  implicit val arbKickCommand =
    Arbitrary[Command.Kick](arbitrary[Nick].map(Command.Kick.apply))

  implicit val arbCommand = Arbitrary[Command](
    Gen.oneOf(
      Gen.const(Command.LookupNick),
      arbitrary[Command.RenameTo],
      arbitrary[Command.Message],
      Gen.const(Command.LookupNames),
      arbitrary[Command.Kick]
    ))
}
