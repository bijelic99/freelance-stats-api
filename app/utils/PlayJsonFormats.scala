package utils

import model.{
  Chart,
  Dashboard,
  PieChart,
  PieData,
  VisualizationData,
  VisualizationLimits
}
import play.api.libs.json.{Format, JsError, JsString, JsSuccess, Json, OFormat}

object PlayJsonFormats {
  import play.api.libs.json.JodaReads._
  import play.api.libs.json.JodaWrites._

  implicit val visualizationLimitFormat: OFormat[VisualizationLimits] =
    Json.format[VisualizationLimits]

  implicit val visualizationData: OFormat[VisualizationData] =
    Json.format[VisualizationData]

  implicit val pieDataFormat: OFormat[PieData] = Json.format[PieData]

  implicit val pieDataTypeFormat: Format[PieChart.DataType] =
    Format[PieChart.DataType](
      fjs = {
        case JsString("Category") => JsSuccess(PieChart.Category)
        case JsString("WorkType") => JsSuccess(PieChart.WorkType)
        case JsString("Language") => JsSuccess(PieChart.Language)
        case JsString("Timezone") => JsSuccess(PieChart.Timezone)
        case _                    => JsError("Type not recognized")
      },
      tjs = {
        case PieChart.Category => JsString("Category")
        case PieChart.WorkType => JsString("WorkType")
        case PieChart.Language => JsString("Language")
        case PieChart.Timezone => JsString("Timezone")
      }
    )

  implicit val pieChartFormat: OFormat[PieChart] =
    Json.format[PieChart]

  implicit val chartFormat: OFormat[Chart] = Json.format[Chart]

  implicit val dashboardFormat: OFormat[Dashboard] = Json.format[Dashboard]
}