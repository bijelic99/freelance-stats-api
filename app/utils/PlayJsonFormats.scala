package utils

import model.{
  BubbleChart,
  Chart,
  ChartData,
  ChartMetadata,
  Dashboard,
  KeyValuePair,
  KeyValueSeqData,
  PieChart,
  Source,
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

  implicit val pieDataEntryFormat: OFormat[KeyValuePair] =
    Json.format[KeyValuePair]

  implicit val chartMetadataFormat: Format[ChartMetadata] =
    Json.format[ChartMetadata]

  implicit val pieDataFormat: OFormat[KeyValueSeqData] =
    Json.format[KeyValueSeqData]

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

  implicit val bubbleDataTypeFormat: Format[BubbleChart.DataType] =
    Format[BubbleChart.DataType](
      fjs = {
        case JsString("AverageNumberOfJobsPerHourPerDay") =>
          JsSuccess(BubbleChart.AverageNumberOfJobsPerHourPerDay)
        case JsString("AverageNumberOfJobsPerDayPerMonth") =>
          JsSuccess(BubbleChart.AverageNumberOfJobsPerDayPerMonth)
        case JsString("AverageNumberOfJobsPerMonthPerYear") =>
          JsSuccess(BubbleChart.AverageNumberOfJobsPerMonthPerYear)
        case _ => JsError("Type not recognized")
      },
      tjs = {
        case BubbleChart.AverageNumberOfJobsPerHourPerDay =>
          JsString("AverageNumberOfJobsPerHourPerDay")
        case BubbleChart.AverageNumberOfJobsPerDayPerMonth =>
          JsString("AverageNumberOfJobsPerDayPerMonth")
        case BubbleChart.AverageNumberOfJobsPerMonthPerYear =>
          JsString("AverageNumberOfJobsPerMonthPerYear")
      }
    )

  implicit val pieChartFormat: OFormat[PieChart] =
    Json.format[PieChart]

  implicit val bubbleChartFormat: OFormat[BubbleChart] =
    Json.format[BubbleChart]

  implicit val chartFormat: OFormat[Chart] = Json.format[Chart]

  implicit val dashboardFormat: OFormat[Dashboard] = Json.format[Dashboard]

  implicit val pieChartDataFormat: Format[KeyValueSeqData] =
    Json.format[KeyValueSeqData]

  implicit val chartDataFormat: Format[ChartData] = Json.format[ChartData]

  implicit val sourceFormat: Format[Source] = Json.format[Source]
}
