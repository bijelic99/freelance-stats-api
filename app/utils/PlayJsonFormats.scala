package utils

import model.{
  BubbleChart,
  Chart,
  ChartData,
  ChartMetadata,
  Dashboard,
  DashboardMetadata,
  Interval,
  KeyMultiValueEntry,
  KeyMultiValueSeqData,
  KeyValuePair,
  KeyValueSeqData,
  LineChart,
  PieChart,
  SearchResponse,
  Source,
  VisualizationData,
  VisualizationLimits
}
import play.api.libs.json.{
  Format,
  JsError,
  JsNull,
  JsNumber,
  JsString,
  JsSuccess,
  JsUndefined,
  Json,
  OFormat
}

object PlayJsonFormats {
  import play.api.libs.json.JodaReads._
  import play.api.libs.json.JodaWrites._

  implicit val visualizationLimitFormat: OFormat[VisualizationLimits] =
    Json.format[VisualizationLimits]

  implicit val visualizationData: OFormat[VisualizationData] =
    Json.format[VisualizationData]

  implicit val pieDataEntryFormat: OFormat[KeyValuePair] =
    Json.format[KeyValuePair]

  implicit val optionDoubleFormat: Format[Option[Double]] =
    Format[Option[Double]](
      fjs = {
        case JsNumber(value) => JsSuccess(Some(value.toDouble))
        case JsNull          => JsSuccess(None)
        case _               => JsError("Type not recognized")
      },
      tjs = {
        case Some(value) => JsNumber(value)
        case None        => JsNull
      }
    )

  implicit val keyMultiValueEntryFormat: OFormat[KeyMultiValueEntry] =
    Json.format[KeyMultiValueEntry]

  implicit val keyMultiValueSeqDataFormat: OFormat[KeyMultiValueSeqData] =
    Json.format[KeyMultiValueSeqData]

  implicit val pieDataFormat: OFormat[KeyValueSeqData] =
    Json.format[KeyValueSeqData]

  implicit val chartMetadataFormat: Format[ChartMetadata] =
    Json.format[ChartMetadata]

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

  implicit val lineDataTypeFormat: Format[LineChart.DataType] =
    Format[LineChart.DataType](
      fjs = {
        case JsString("FixedPriceJobValueInTime") =>
          JsSuccess(LineChart.FixedPriceJobValueInTime)
        case JsString("HourlyJobValueInTime") =>
          JsSuccess(LineChart.HourlyJobValueInTime)
        case JsString("NumberOfFixedPriceJobsInTime") =>
          JsSuccess(LineChart.NumberOfFixedPriceJobsInTime)
        case JsString("NumberOfHourlyJobsInTime") =>
          JsSuccess(LineChart.NumberOfHourlyJobsInTime)
        case JsString("NumberOfJobsInTime") =>
          JsSuccess(LineChart.NumberOfJobsInTime)
        case _ => JsError("Type not recognized")
      },
      tjs = {
        case LineChart.FixedPriceJobValueInTime =>
          JsString("FixedPriceJobValueInTime")
        case LineChart.HourlyJobValueInTime => JsString("HourlyJobValueInTime")
        case LineChart.NumberOfFixedPriceJobsInTime =>
          JsString("NumberOfFixedPriceJobsInTime")
        case LineChart.NumberOfHourlyJobsInTime =>
          JsString("NumberOfHourlyJobsInTime")
        case LineChart.NumberOfJobsInTime =>
          JsString("NumberOfJobsInTime")
      }
    )

  implicit val intervalFormat: Format[Interval] = Format[Interval](
    fjs = {
      case JsString("Hour")    => JsSuccess(Interval.Hour)
      case JsString("Day")     => JsSuccess(Interval.Day)
      case JsString("Week")    => JsSuccess(Interval.Week)
      case JsString("Month")   => JsSuccess(Interval.Month)
      case JsString("Quarter") => JsSuccess(Interval.Quarter)
      case JsString("Year")    => JsSuccess(Interval.Year)
      case _                   => JsError("Type not recognized")
    },
    tjs = {
      case Interval.Hour    => JsString("Hour")
      case Interval.Day     => JsString("Day")
      case Interval.Week    => JsString("Week")
      case Interval.Month   => JsString("Month")
      case Interval.Quarter => JsString("Quarter")
      case Interval.Year    => JsString("Year")
    }
  )

  implicit val pieChartFormat: OFormat[PieChart] =
    Json.format[PieChart]

  implicit val bubbleChartFormat: OFormat[BubbleChart] =
    Json.format[BubbleChart]

  implicit val lineChartFormat: OFormat[LineChart] =
    Json.format[LineChart]

  implicit val chartFormat: OFormat[Chart] = Json.format[Chart]

  implicit val dashboardFormat: OFormat[Dashboard] = Json.format[Dashboard]

  implicit val pieChartDataFormat: Format[KeyValueSeqData] =
    Json.format[KeyValueSeqData]

  implicit val chartDataFormat: Format[ChartData] = Json.format[ChartData]

  implicit val sourceFormat: Format[Source] = Json.format[Source]

  implicit val dashboardMetadataFormat: Format[DashboardMetadata] =
    Json.format[DashboardMetadata]

  implicit val dashboardSearchResponseFormat
      : Format[SearchResponse[DashboardMetadata]] =
    Json.format[SearchResponse[DashboardMetadata]]
}
