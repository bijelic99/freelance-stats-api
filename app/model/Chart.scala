package model

import org.joda.time.DateTime

sealed trait Chart {
  def id: String

  def name: String

  def dateFrom: Option[DateTime]

  def dateTo: Option[DateTime]

  def source: Option[String]

  def visualizationData: VisualizationData
}

object Chart {
  implicit class ChartOps(chart: Chart) {
    def setId(id: String): Chart = chart match {
      case chart: PieChart    => chart.copy(id = id)
      case chart: BubbleChart => chart.copy(id = id)
    }

    def setCoordinates(coordinates: (Int, Int)): Chart = chart match {
      case chart: PieChart =>
        chart.copy(visualizationData =
          chart.visualizationData.copy(x = coordinates._1, y = coordinates._2)
        )
      case chart: BubbleChart =>
        chart.copy(visualizationData =
          chart.visualizationData.copy(x = coordinates._1, y = coordinates._2)
        )
    }
  }
}

case class PieChart(
    id: String,
    name: String,
    dateFrom: Option[DateTime],
    dateTo: Option[DateTime],
    source: Option[String],
    visualizationData: VisualizationData,
    dataType: PieChart.DataType
) extends Chart

object PieChart {
  sealed trait DataType
  case object WorkType extends DataType
  case object Category extends DataType
  case object Language extends DataType
  case object Timezone extends DataType
}

case class BubbleChart(
    id: String,
    name: String,
    dateFrom: Option[DateTime],
    dateTo: Option[DateTime],
    source: Option[String],
    visualizationData: VisualizationData,
    dataType: BubbleChart.DataType
) extends Chart

object BubbleChart {
  sealed trait DataType extends Product
  case object AverageNumberOfJobsPerHourPerDay extends DataType
  case object AverageNumberOfJobsPerDayPerMonth extends DataType
  case object AverageNumberOfJobsPerMonthPerYear extends DataType
}
