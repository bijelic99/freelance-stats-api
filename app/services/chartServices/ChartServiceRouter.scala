package services.chartServices

import model.{Chart, ChartData, PieChart}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChartServiceRouter @Inject() (
    pieChartService: PieChartService
)(implicit ec: ExecutionContext)
    extends ChartService[Chart] {
  override def getData(chart: Chart): Future[ChartData] = chart match {
    case chart: PieChart =>
      pieChartService.getData(chart)
  }
}
