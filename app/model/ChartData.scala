package model

sealed trait ChartData {
  def chartId: String
}

case class PieData(
    chartId: String,
    data: Map[String, Double]
) extends ChartData
