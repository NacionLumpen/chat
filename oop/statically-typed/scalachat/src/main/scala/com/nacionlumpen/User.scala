package com.nacionlumpen

object User {
  def isMalformed(name: String) = {
    val wellFormedPattern = """[\w-]{1,10}"""
    !name.matches(wellFormedPattern)
  }

}
