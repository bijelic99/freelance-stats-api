package services.chartServices

import model.{Chart, ChartData}

import scala.concurrent.Future

trait ChartDataService[T <: Chart] {
  def getData(chart: T): Future[ChartData]
}
