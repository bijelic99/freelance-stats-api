package model

import play.api.Configuration

case class VisualizationLimits(
    minW: Int,
    minH: Int,
    maxW: Int,
    maxH: Int
)

object VisualizationLimits {
  def apply(configuration: Configuration): VisualizationLimits =
    VisualizationLimits.apply(
      configuration.get[Int]("minW"),
      configuration.get[Int]("minH"),
      configuration.get[Int]("maxW"),
      configuration.get[Int]("maxH")
    )
}
