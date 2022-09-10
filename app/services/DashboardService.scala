package services

import cats.data.OptionT
import cats.implicits.catsSyntaxOptionId
import com.freelanceStats.commons.models.User
import model.{
  AccessForbiddenException,
  Chart,
  ChartData,
  Dashboard,
  VisualizationData
}
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

  def getChart(
      dashboardId: String,
      chartId: String
  )(implicit user: User): Future[Option[Chart]] = {
    repository
      .get(dashboardId)
      .map(_.flatMap {
        case dashboard
            if (dashboard.usersWithAccess :+ dashboard.ownerId)
              .contains(user.id) =>
          dashboard.charts
            .find(_.id.equals(chartId))
        case _ =>
          val message =
            s"Access denied, user with id of: '${user.id}' tried accessing '$dashboardId'/'$chartId'"
          log.warn(message)
          throw AccessForbiddenException(message)
      })
  }

  def addChart(dashboardId: String, chart: Chart)(implicit
      user: User
  ): Future[Option[Chart]] =
    repository
      .get(dashboardId)
      .flatMap {
        case Some(dashboard)
            if (dashboard.usersWithAccess :+ dashboard.ownerId).contains(
              user.id
            ) =>
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
        case Some(_) =>
          val message =
            s"Access denied, user with id of: '${user.id}' tried accessing '$dashboardId'/'${chart.id}'"
          log.warn(message)
          throw AccessForbiddenException(message)
        case None => Future.successful(None)
      }

  def userHasAccessToDashboard(
      dashboardId: String,
      user: User
  ): Future[Option[Boolean]] =
    repository
      .get(dashboardId)
      .map(
        _.map(dashboard =>
          (dashboard.usersWithAccess :+ dashboard.ownerId).contains(user.id)
        )
      )

  def userHasAccessToDashboard2[T](dashboardId: String, user: User)(
      accessGranted: () => Future[T],
      accessDenied: () => Future[T],
      onNotFound: () => Future[T]
  ): Future[T] =
    repository
      .get(dashboardId)
      .flatMap(_.fold(onNotFound()) {
        case dashboard
            if (dashboard.usersWithAccess :+ dashboard.ownerId)
              .contains(user.id) =>
          accessGranted()
        case _ =>
          accessDenied()
      })

  def logAndThrowOnAccessDenied[T](
      userId: String,
      dashboardId: String,
      chartId: Option[String] = None
  ): () => T = {
    val message =
      s"Access denied, user with id of: '$userId' tried accessing '$dashboardId'${chartId
        .map(cid => s", chart: '$cid'")
        .getOrElse("")}"
    log.warn(message)
    throw AccessForbiddenException(message)
  }

  def updateChart(dashboardId: String, chart: Chart)(implicit
      user: User
  ): Future[Option[Chart]] =
    userHasAccessToDashboard2(dashboardId, user)(
      accessGranted = () => {
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
      },
      accessDenied =
        logAndThrowOnAccessDenied(user.id, dashboardId, chart.id.some),
      onNotFound = () => Future.successful(None)
    )

  def deleteChart(dashboardId: String, chartId: String)(implicit
      user: User
  ): Future[Boolean] =
    userHasAccessToDashboard2(dashboardId, user)(
      accessGranted = () => {
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
      },
      accessDenied =
        logAndThrowOnAccessDenied(user.id, dashboardId, chartId.some),
      onNotFound = () => Future.successful(false)
    )

  def getDashboardChartsData(
      dashboardId: String
  )(implicit user: User): Future[Map[String, ChartData]] =
    repository
      .get(dashboardId)
      .flatMap {
        case Some(dashboard)
            if (dashboard.usersWithAccess :+ dashboard.ownerId).contains(
              user.id
            ) || dashboard.public =>
          Future
            .sequence {
              dashboard.charts.map { chart =>
                chartServiceRouter
                  .getData(chart)
                  .map(chart.id -> _)
              }
            }
            .map(_.toMap)
        case Some(_) =>
          logAndThrowOnAccessDenied(user.id, dashboardId)()
        case None =>
          val message = s"Dashboard with id of: '$dashboardId' not found"
          log.error(message)
          throw new Exception(message)
      }

  def addDashboard(
      dashboard: Dashboard
  )(implicit user: User): Future[Dashboard] =
    repository
      .add(dashboard.copy(id = UUID.randomUUID().toString, ownerId = user.id))
      .flatMap { dashboard =>
        dashboardIndexService
          .indexDashboard(dashboard)
          .map(_ => dashboard)
      }

  def updateDashboard(dashboard: Dashboard)(implicit
      user: User
  ): Future[Option[Dashboard]] =
    userHasAccessToDashboard2(dashboard.id, user)(
      accessGranted = () => {
        repository
          .update(dashboard)
          .flatMap(_.map { dashboard =>
            dashboardIndexService
              .indexDashboard(dashboard)
              .map(_ => Some(dashboard))
          }.getOrElse(Future.successful(None)))
      },
      accessDenied = logAndThrowOnAccessDenied(user.id, dashboard.id),
      onNotFound = () => Future.successful(None)
    )

  def deleteDashboard(id: String)(implicit user: User): Future[Boolean] =
    userHasAccessToDashboard2(id, user)(
      accessGranted = () => {
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
      },
      accessDenied = logAndThrowOnAccessDenied(user.id, id),
      onNotFound = () => Future.successful(false)
    )

  def updateChartsVisualizationData(
      dashboardId: String,
      chartIdVisualizationDataMap: Map[String, VisualizationData]
  )(implicit user: User): Future[Option[Dashboard]] =
    userHasAccessToDashboard2(dashboardId, user)(
      accessGranted = () => {
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
      },
      accessDenied = logAndThrowOnAccessDenied(user.id, dashboardId),
      onNotFound = () => Future.successful(None)
    )
}
