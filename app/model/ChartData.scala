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

case class KeyValueMatrixRow(name: String, elements: Seq[KeyValuePair])

case class KeyValueMatrixData(
    chartId: String,
    rows: Seq[KeyValueMatrixRow]
) extends ChartData
