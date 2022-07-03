package controllers

import model.Chart
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import play.api.mvc._
import repositories.dashboardRepository.DashboardRepository
import services.DashboardService

import java.util.UUID
import javax.inject._
import scala.concurrent.ExecutionContext

class ChartController @Inject() (
    val controllerComponents: ControllerComponents,
    dashboardRepository: DashboardRepository,
    dashboardService: DashboardService
)(implicit
    ec: ExecutionContext
) extends BaseController {

  import utils.PlayJsonFormats._

  val log: Logger = LoggerFactory.getLogger(classOf[ChartController])

  def get(dashboardId: String, chartId: String): Action[AnyContent] =
    Action.async { implicit request: Request[AnyContent] =>
      dashboardRepository
        .getChart(dashboardId, chartId)
        .map {
          case Some(chart) => Ok(Json.toJson(chart))
          case None        => NotFound
        }
        .recover { t =>
          log.error("Unexpected error while getting the chart", t)
          InternalServerError
        }
    }

  def post(dashboardId: String): Action[Chart] =
    Action.async(parse.json[Chart]) { implicit request: Request[Chart] =>
      dashboardRepository
        .addChart(dashboardId, request.body.setId(UUID.randomUUID().toString))
        .map {
          case Some(chart) => Created(Json.toJson(chart))
          case None        => NotFound
        }
        .recover { t =>
          log.error("Unexpected error while adding chart", t)
          InternalServerError
        }
    }

  def put(dashboardId: String): Action[Chart] =
    Action.async(parse.json[Chart]) { implicit request: Request[Chart] =>
      dashboardRepository
        .updateChart(dashboardId, request.body)
        .map {
          case Some(chart) => Ok(Json.toJson(chart))
          case None        => NotFound
        }
        .recover { t =>
          log.error("Unexpected error while updating chart", t)
          InternalServerError
        }
    }

  def delete(dashboardId: String, chartId: String): Action[AnyContent] =
    Action.async { implicit request: Request[AnyContent] =>
      dashboardRepository
        .removeChart(dashboardId, chartId)
        .map {
          case true  => Ok
          case false => NotFound
        }
        .recover { t =>
          log.error("Unexpected error while deleting chart", t)
          InternalServerError
        }
    }
}
