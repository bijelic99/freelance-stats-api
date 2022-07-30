package controllers

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
    dashboardIndexService: DashboardIndexService
)(implicit
    ec: ExecutionContext
) extends BaseController {

  import utils.PlayJsonFormats._

  val log: Logger = LoggerFactory.getLogger(classOf[DashboardController])

  def get(id: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
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

  def post(): Action[Dashboard] = Action.async(parse.json[Dashboard]) {
    implicit request: Request[Dashboard] =>
      dashboardService
        .addDashboard(request.body)
        .map(dashboard => Created(Json.toJson(dashboard)))
        .recover { t =>
          log.error("Unexpected error while adding dashboard", t)
          InternalServerError
        }
  }

  def put(): Action[Dashboard] = Action.async(parse.json[Dashboard]) {
    implicit request: Request[Dashboard] =>
      dashboardService
        .updateDashboard(request.body)
        .map {
          case Some(dashboard) => Ok(Json.toJson(dashboard))
          case None            => NotFound
        }
        .recover { t =>
          log.error("Unexpected error while editing dashboard", t)
          InternalServerError
        }
  }

  def delete(id: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      dashboardService
        .deleteDashboard(id)
        .map {
          case true  => Ok
          case false => NotFound
        }
        .recover { t =>
          log.error("Unexpected error while deleting dashboard", t)
          InternalServerError
        }
  }

  def getChartData(id: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      dashboardService
        .getDashboardChartsData(id)
        .map(data => Ok(Json.toJson(data)))
        .recover { t =>
          log.error("Unexpected error while getting chart data", t)
          InternalServerError
        }
  }

  def reindex(): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      dashboardIndexService.reindexDashboards
        .map(_ => Ok)
        .recover(t => InternalServerError(t.getMessage))
  }

  def search(term: Option[String], size: Int, from: Int): Action[AnyContent] =
    Action.async { implicit request: Request[AnyContent] =>
      dashboardIndexService
        .searchDashboards(term, None, size, from)
        .map(Json.toJson(_))
        .map(Ok(_))
        .recover { t =>
          val message = "Unexpected error while searching for dashboards"
          log.error(message, t)
          InternalServerError(message)
        }
    }
}
