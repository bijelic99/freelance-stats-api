package model

sealed trait ChartData

case class PieData(
    data: Map[String, Double]
) extends ChartData
