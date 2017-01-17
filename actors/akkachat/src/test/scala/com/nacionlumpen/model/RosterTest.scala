package com.nacionlumpen.model

import scalaz.NonEmptyList

import com.nacionlumpen.model.Response.PartialRoster
import com.nacionlumpen.model.Response.Roster
import org.scalatest.{FlatSpec, Matchers}

class RosterTest extends FlatSpec with Matchers {

  val nick = Nick("anon123456")

  "A long list of nicks" should "be converted into a series of partial and final rosters" in {
    val nicks = List.fill(100)(nick)
    val partialRoster = PartialRoster(NonEmptyList(nick, Seq.fill(21)(nick): _*))

    Roster.from(nicks) shouldBe (List(partialRoster, partialRoster, partialRoster, partialRoster), Roster(
      List.fill(12)(nick)))
  }

  "A sort list of nick" should "be converted to just a complete roster" in {
    val nicks = List.fill(5)(nick)
    Roster.from(nicks) shouldBe (List.empty, Roster(nicks))
  }
}
