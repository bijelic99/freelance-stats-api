package services.chartServices

import model.{Chart, ChartData, PieChart}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChartDataServiceRouter @Inject() (
    pieChartService: PieChartDataService
)(implicit ec: ExecutionContext)
    extends ChartDataService[Chart] {
  override def getData(chart: Chart): Future[ChartData] = chart match {
    case chart: PieChart =>
      pieChartService.getData(chart)
  }
}
