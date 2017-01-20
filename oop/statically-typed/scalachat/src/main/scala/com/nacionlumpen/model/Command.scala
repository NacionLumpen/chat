package com.nacionlumpen.model

import scala.util.matching.Regex

sealed trait Command {
  def regex: Regex
}

case class Nick(name: String) extends Command {
  override def regex: Regex = ???
}

