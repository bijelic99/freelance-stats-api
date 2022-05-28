package util

import model.VisualizationLimits
import play.api.libs.json.{Json, OWrites}

object PlayJsonFormats {
  implicit val visualizationLimitWrites: OWrites[VisualizationLimits] = OWrites[VisualizationLimits]{
    case VisualizationLimits(chartClass, minW, minH, maxW, maxH) =>
      Json.obj(
        "chartClass" -> chartClass.getName,
        "minW" -> minW,
        "minH" -> minH,
        "maxW" -> maxW,
        "maxH" -> maxH
      )
  }
}
