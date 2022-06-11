package util

import model.VisualizationLimits
import play.api.libs.json.{Json, OFormat, OWrites}

object PlayJsonFormats {
  implicit val visualizationLimitFormat: OFormat[VisualizationLimits] =
    Json.format[VisualizationLimits]
}
