package services

import model.ChartData
import org.slf4j.{Logger, LoggerFactory}
import repositories.dashboardRepository.DashboardRepository
import services.chartServices.ChartServiceRouter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DashboardService @Inject() (
    repository: DashboardRepository,
    chartServiceRouter: ChartServiceRouter
)(implicit ec: ExecutionContext) {

  private val log: Logger = LoggerFactory.getLogger(classOf[DashboardService])

  def getDashboardChartsData(
      dashboardId: String
  ): Future[Map[String, ChartData]] =
    repository
      .get(dashboardId)
      .flatMap {
        case Some(dashboard) =>
          Future
            .sequence {
              dashboard.charts.map { chart =>
                chartServiceRouter
                  .getData(chart)
                  .map(chart.id -> _)
              }
            }
            .map(_.toMap)
        case None =>
          val message = s"Dashboard with id of: '$dashboardId' not found"
          log.error(message)
          throw new Exception(message)
      }
}
