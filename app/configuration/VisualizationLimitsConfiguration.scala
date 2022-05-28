package configuration

import model.{Chart, VisualizationLimits}
import play.api.Configuration

import javax.inject.Inject

class VisualizationLimitsConfiguration @Inject()(configuration: Configuration) {
  val limits: Seq[VisualizationLimits] = {
    configuration
      .getOptional[Seq[Configuration]]("charts.visualization.limits")
      .getOrElse(Nil)
      .map{ limitConfig =>
        VisualizationLimits(
          chartClass = Class.forName(limitConfig.get[String]("chartClass")).asInstanceOf[Class[_ <: Chart[_]]],
          minW = limitConfig.get[Int]("minW"),
          minH = limitConfig.get[Int]("minH"),
          maxW = limitConfig.get[Int]("maxW"),
          maxH = limitConfig.get[Int]("maxH")
        )
      }
  }
}
