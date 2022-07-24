package services.chartServices

import model.{BubbleChart, Chart, ChartData, LineChart, PieChart}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChartDataServiceRouter @Inject() (
    pieChartService: PieChartDataService,
    bubbleChartService: BubbleChartDataService,
    lineChartDataService: LineChartDataService
)(implicit ec: ExecutionContext)
    extends ChartDataService[Chart] {
  override def getData(chart: Chart): Future[ChartData] = chart match {
    case chart: PieChart =>
      pieChartService.getData(chart)
    case chart: BubbleChart =>
      bubbleChartService.getData(chart)
    case chart: LineChart =>
      lineChartDataService.getData(chart)
  }
}
