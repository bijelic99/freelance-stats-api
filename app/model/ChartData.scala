package model

sealed trait ChartData {
  def chartId: String
}

case class PieDataEntry(
    name: String,
    value: Double
)

case class PieData(
    chartId: String,
    data: Seq[PieDataEntry]
) extends ChartData
