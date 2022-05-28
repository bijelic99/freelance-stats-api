package model

import model.ChartData.{Name, Value}

sealed trait ChartData

object ChartData {

  type Name = String
  type Value = String

}

case class PieData(
    data: Map[Name, Value]
) extends ChartData
