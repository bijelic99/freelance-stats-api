package services

import cats.data.OptionT
import model.{Chart, ChartData, Dashboard, VisualizationData}
import org.slf4j.{Logger, LoggerFactory}
import repositories.dashboardRepository.DashboardRepository
import services.chartServices.ChartDataServiceRouter
import services.dashboardIndexService.DashboardIndexService

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DashboardService @Inject() (
    repository: DashboardRepository,
    chartServiceRouter: ChartDataServiceRouter,
    dashboardIndexService: DashboardIndexService
)(implicit ec: ExecutionContext) {

  private val log: Logger = LoggerFactory.getLogger(classOf[DashboardService])

  private def calculateCoordinates(
      dashboard: Dashboard,
      chart: Chart
  ): (Int, Int) = {
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
          repository
            .addChart(
              dashboardId,
              chart
                .setId(UUID.randomUUID().toString)
                .setCoordinates(calculateCoordinates(dashboard, chart))
            )
            .flatMap { maybeChart =>
              repository
                .get(dashboardId)
                .map(_.get)
                .flatMap(dashboardIndexService.indexDashboard)
                .map(_ => maybeChart)
            }
        case None => Future.successful(None)
      }

  def updateChart(dashboardId: String, chart: Chart): Future[Option[Chart]] =
    repository
      .updateChart(dashboardId, chart)
      .flatMap {
        case Some(chart) =>
          repository
            .get(dashboardId)
            .map(_.get)
            .flatMap(dashboardIndexService.indexDashboard)
            .map(_ => Some(chart))
        case None =>
          Future.successful(None)
      }

  def deleteChart(dashboardId: String, chartId: String): Future[Boolean] =
    repository
      .removeChart(dashboardId, chartId)
      .flatMap {
        case true =>
          repository
            .get(dashboardId)
            .map(_.get)
            .flatMap(dashboardIndexService.indexDashboard)
            .map(_ => true)
        case false =>
          Future.successful(false)
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

  def addDashboard(dashboard: Dashboard): Future[Dashboard] =
    repository
      .add(dashboard.copy(id = UUID.randomUUID().toString))
      .flatMap { dashboard =>
        dashboardIndexService
          .indexDashboard(dashboard)
          .map(_ => dashboard)
      }

  def updateDashboard(dashboard: Dashboard): Future[Option[Dashboard]] =
    repository
      .update(dashboard)
      .flatMap(_.map { dashboard =>
        dashboardIndexService
          .indexDashboard(dashboard)
          .map(_ => Some(dashboard))
      }.getOrElse(Future.successful(None)))

  def deleteDashboard(id: String): Future[Boolean] =
    repository
      .delete(id)
      .flatMap {
        case true =>
          repository
            .get(id)
            .map(_.get)
            .flatMap(dashboard =>
              dashboardIndexService
                .indexDashboard(dashboard.copy(deleted = true))
            )
            .map(_ => true)
        case false =>
          Future.successful(false)
      }

  def updateChartsVisualizationData(
      dashboardId: String,
      chartIdVisualizationDataMap: Map[String, VisualizationData]
  ): Future[Option[Dashboard]] =
    OptionT(
      repository
        .get(dashboardId)
    ).flatMap { dashboard =>
      val updatedCharts = dashboard.charts.map { chart =>
        chartIdVisualizationDataMap
          .get(chart.id)
          .fold(chart)(chart.setVisualizationData(_))
      }
      OptionT(
        repository.update(dashboard.copy(charts = updatedCharts))
      )
    }.value
}
