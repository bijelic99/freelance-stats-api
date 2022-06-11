package model

sealed trait ChartData {
  def dashboardId: String
  def chartId: String
}

case class PieData(
    dashboardId: String,
    chartId: String,
    data: Map[String, Double]
) extends ChartData
