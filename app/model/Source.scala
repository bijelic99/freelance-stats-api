package model

import play.api.Configuration

case class Source(
    id: String,
    name: String
)

object Source {
  def apply(configuration: Configuration): Source =
    Source.apply(
      configuration.get[String]("id"),
      configuration.get[String]("name")
    )
}
