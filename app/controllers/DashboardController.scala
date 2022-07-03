package controllers

import model.Dashboard
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import play.api.mvc._
import repositories.dashboardRepository.DashboardRepository
import services.DashboardService

import java.util.UUID
import javax.inject._
import scala.concurrent.ExecutionContext

class DashboardController @Inject() (
    val controllerComponents: ControllerComponents,
    dashboardRepository: DashboardRepository,
    dashboardService: DashboardService
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
      dashboardRepository
        .add(request.body.copy(id = UUID.randomUUID().toString))
        .map(dashboard => Created(Json.toJson(dashboard)))
        .recover { t =>
          log.error("Unexpected error while adding dashboard", t)
          InternalServerError
        }
  }

  def put(): Action[Dashboard] = Action.async(parse.json[Dashboard]) {
    implicit request: Request[Dashboard] =>
      dashboardRepository
        .update(request.body)
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
      dashboardRepository
        .delete(id)
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
}
