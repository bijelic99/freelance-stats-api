package model

import play.api.Configuration

case class ChartMetadata(
    id: String,
    `class`: String,
    name: String,
    visualizationLimits: VisualizationLimits
)

object ChartMetadata {
  def apply(configuration: Configuration): ChartMetadata =
    ChartMetadata.apply(
      configuration.get[String]("id"),
      configuration.get[String]("class"),
      configuration.get[String]("name"),
      VisualizationLimits(
        configuration.get[Configuration]("visualizationLimits")
      )
    )
}
