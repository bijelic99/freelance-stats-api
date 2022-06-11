package model

import org.joda.time.DateTime

sealed trait Chart {
  def id: String

  def dashboardId: String

  def name: String

  def dateFrom: DateTime

  def dateTo: Option[DateTime]

  def source: Option[String]

  def visualizationData: VisualizationData
}

case class PieChart(
    id: String,
    dashboardId: String,
    name: String,
    dateFrom: DateTime,
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
