package model

sealed trait ChartData {
  def chartId: String
}

case class KeyValuePair(
    name: String,
    value: Double
)

case class KeyValueSeqData(
    chartId: String,
    data: Seq[KeyValuePair]
) extends ChartData
