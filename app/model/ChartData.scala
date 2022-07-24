package model

sealed trait ChartData {
  def chartId: String
}

case class KeyValuePair(
    key: String,
    value: Double
)

case class KeyValueSeqData(
    chartId: String,
    data: Seq[KeyValuePair]
) extends ChartData

case class KeyMultiValueEntry(
    key: String,
    values: Map[String, Double]
)

case class KeyMultiValueSeqData(
    chartId: String,
    data: Seq[KeyMultiValueEntry]
) extends ChartData
