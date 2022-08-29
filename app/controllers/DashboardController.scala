package controllers

import com.freelanceStats.jwtAuth.actions.JwtAuthActionBuilder
import com.freelanceStats.jwtAuth.models.AuthenticatedRequest
import model.Dashboard
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import play.api.mvc._
import repositories.dashboardRepository.DashboardRepository
import services.DashboardService
import services.dashboardIndexService.DashboardIndexService

import javax.inject._
import scala.concurrent.ExecutionContext

class DashboardController @Inject() (
    val controllerComponents: ControllerComponents,
    dashboardRepository: DashboardRepository,
    dashboardService: DashboardService,
    dashboardIndexService: DashboardIndexService,
    authActionBuilder: JwtAuthActionBuilder
)(implicit
    ec: ExecutionContext
) extends BaseController {

  import utils.PlayJsonFormats._

  val log: Logger = LoggerFactory.getLogger(classOf[DashboardController])

  def get(id: String): Action[AnyContent] = authActionBuilder.async {
    implicit request: AuthenticatedRequest[AnyContent] =>
      dashboardRepository
        .get(id)
        .map {
          case Some(dashboard) => Ok(Json.toJson(dashboard))
          case None =>
            NotFound
        }
        .recover { t =>
          log.error("Unexpected error while getting dashboard", t)
          InternalServerError
        }
  }

  def post(): Action[Dashboard] =
    authActionBuilder.async(parse.json[Dashboard]) {
      implicit request: AuthenticatedRequest[Dashboard] =>
        dashboardService
          .addDashboard(request.body)(request.user)
          .map(dashboard => Created(Json.toJson(dashboard)))
          .recover { t =>
            log.error("Unexpected error while adding dashboard", t)
            InternalServerError
          }
    }

  def put(): Action[Dashboard] =
    authActionBuilder.async(parse.json[Dashboard]) {
      implicit request: AuthenticatedRequest[Dashboard] =>
        dashboardService
          .updateDashboard(request.body)(request.user)
          .map {
            case Some(dashboard) => Ok(Json.toJson(dashboard))
            case None            => NotFound
          }
          .recover { t =>
            log.error("Unexpected error while editing dashboard", t)
            InternalServerError
          }
    }

  def delete(id: String): Action[AnyContent] = authActionBuilder.async {
    implicit request: AuthenticatedRequest[AnyContent] =>
      dashboardService
        .deleteDashboard(id)(request.user)
        .map {
          case true  => Ok
          case false => NotFound
        }
        .recover { t =>
          log.error("Unexpected error while deleting dashboard", t)
          InternalServerError
        }
  }

  def getChartData(id: String): Action[AnyContent] = authActionBuilder.async {
    implicit request: AuthenticatedRequest[AnyContent] =>
      dashboardService
        .getDashboardChartsData(id)(request.user)
        .map(data => Ok(Json.toJson(data)))
        .recover { t =>
          log.error("Unexpected error while getting chart data", t)
          InternalServerError
        }
  }

  def reindex(): Action[AnyContent] = authActionBuilder.async {
    implicit request: AuthenticatedRequest[AnyContent] =>
      dashboardIndexService.reindexDashboards
        .map(_ => Ok)
        .recover(t => InternalServerError(t.getMessage))
  }

  def search(term: Option[String], size: Int, from: Int): Action[AnyContent] =
    authActionBuilder.async {
      implicit request: AuthenticatedRequest[AnyContent] =>
        dashboardIndexService
          .searchDashboards(term, Some(request.user.id), size, from)
          .map(Json.toJson(_))
          .map(Ok(_))
          .recover { t =>
            val message = "Unexpected error while searching for dashboards"
            log.error(message, t)
            InternalServerError(message)
          }
    }
}
