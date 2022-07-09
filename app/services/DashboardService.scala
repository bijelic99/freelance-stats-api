package services

import model.{Chart, ChartData, Dashboard}
import org.slf4j.{Logger, LoggerFactory}
import repositories.dashboardRepository.DashboardRepository
import services.chartServices.ChartDataServiceRouter

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DashboardService @Inject() (
    repository: DashboardRepository,
    chartServiceRouter: ChartDataServiceRouter
)(implicit ec: ExecutionContext) {

  private val log: Logger = LoggerFactory.getLogger(classOf[DashboardService])

  private def calculateCoordinates(dashboard: Dashboard, chart: Chart): (Int, Int) = {
    for {
      maxY <- dashboard.charts
        .maxByOption(_.visualizationData.y)
        .map(_.visualizationData.y)
      lastRow = dashboard.charts.filter(
        _.visualizationData.y.equals(maxY)
      )
      lastChartInRow <- lastRow.maxByOption(_.visualizationData.x)
      lastTakenX =
        lastChartInRow.visualizationData.x + lastChartInRow.visualizationData.w - 1
      lastTakenY = lastRow.map { c =>
        c.visualizationData.y + c.visualizationData.h - 1
      }.max
    } yield
      if (lastTakenX + chart.visualizationData.w < 5)
        (lastTakenX + 1, maxY)
      else (0, lastTakenY + 1)
  }.getOrElse((0, 0))

  def addChart(dashboardId: String, chart: Chart): Future[Option[Chart]] =
    repository
      .get(dashboardId)
      .flatMap {
        case Some(dashboard) =>
          repository.addChart(
            dashboardId,
            chart.setId(UUID.randomUUID().toString).setCoordinates(calculateCoordinates(dashboard, chart))
          )
        case None => Future.successful(None)
      }

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
