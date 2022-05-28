package model

import akka.http.scaladsl.model.DateTime

sealed trait Chart[DataType <: ChartData] {
  def id: String

  def dashboardId: String

  def name: String

  def dateFrom: DateTime

  def dateTo: Option[DateTime]

  def visualizationData: VisualizationData

  def data: Option[DataType]
}

object Chart {
  case class Pie(
      id: String,
      dashboardId: String,
      name: String,
      dateFrom: DateTime,
      dateTo: Option[DateTime],
      visualizationData: VisualizationData,
      data: Option[PieData] = None
  ) extends Chart[PieData]
}

