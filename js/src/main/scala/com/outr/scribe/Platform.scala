package com.outr.scribe

import scala.scalajs.js.Date

object Platform {
  val LineSeparator = "\n"

  private val replacements = Map(
    "%1$tY" -> ((d: Date) => d.getFullYear().toString),
    "%1$tm" -> ((d: Date) => d.getMonth().toString),
    "%1$td" -> ((d: Date) => d.getDay().toString),
    "%1$tT" -> ((d: Date) => s"${d.getHours()}:${d.getMinutes()}:${d.getSeconds()}"),
    "%1$tL" -> ((d: Date) => d.getMilliseconds().toString)
  )

  def formatDate(pattern: String, timestamp: Long): String = {
    val date = new Date(timestamp.toDouble)
    val standardPattern = replacements.foldLeft(pattern) {
      case (p, (original, update)) => p.replaceAllLiterally(original, update(date))
    }
    date.formatted(standardPattern)
  }
}