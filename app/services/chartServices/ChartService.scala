package services.chartServices

import model.{Chart, ChartData}

import scala.concurrent.Future

trait ChartService[T <: Chart] {
  def getData(chart: T): Future[ChartData]
}
