package controllers

import com.freelanceStats.jwtAuth.actions.JwtAuthActionBuilder
import com.freelanceStats.jwtAuth.models.AuthenticatedRequest
import model.{AccessForbiddenException, Chart, VisualizationData}
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
    dashboardService: DashboardService,
    authActionBuilder: JwtAuthActionBuilder
)(implicit
    ec: ExecutionContext
) extends BaseController {

  import utils.PlayJsonFormats._

  val log: Logger = LoggerFactory.getLogger(classOf[ChartController])

  def get(dashboardId: String, chartId: String): Action[AnyContent] =
    authActionBuilder.async {
      implicit request: AuthenticatedRequest[AnyContent] =>
        dashboardService
          .getChart(dashboardId, chartId)(request.user)
          .map {
            case Some(chart) => Ok(Json.toJson(chart))
            case None        => NotFound
          }
          .recover {
            case _: AccessForbiddenException =>
              Forbidden
            case t =>
              log.error("Unexpected error while getting the chart", t)
              InternalServerError
          }
    }

  def post(dashboardId: String): Action[Chart] =
    authActionBuilder.async(parse.json[Chart]) {
      implicit request: AuthenticatedRequest[Chart] =>
        dashboardService
          .addChart(
            dashboardId,
            request.body.setId(UUID.randomUUID().toString)
          )(request.user)
          .map {
            case Some(chart) => Created(Json.toJson(chart))
            case None        => NotFound
          }
          .recover {
            case _: AccessForbiddenException =>
              Forbidden
            case t =>
              log.error("Unexpected error while adding chart", t)
              InternalServerError
          }
    }

  def put(dashboardId: String): Action[Chart] =
    authActionBuilder.async(parse.json[Chart]) {
      implicit request: AuthenticatedRequest[Chart] =>
        dashboardService
          .updateChart(dashboardId, request.body)(request.user)
          .map {
            case Some(chart) => Ok(Json.toJson(chart))
            case None        => NotFound
          }
          .recover {
            case _: AccessForbiddenException =>
              Forbidden
            case t =>
              log.error("Unexpected error while adding chart", t)
              InternalServerError
          }
    }

  def delete(dashboardId: String, chartId: String): Action[AnyContent] =
    authActionBuilder.async {
      implicit request: AuthenticatedRequest[AnyContent] =>
        dashboardService
          .deleteChart(dashboardId, chartId)(request.user)
          .map {
            case true  => Ok
            case false => NotFound
          }
          .recover {
            case _: AccessForbiddenException =>
              Forbidden
            case t =>
              log.error("Unexpected error while adding chart", t)
              InternalServerError
          }
    }

  def visualizationDataPut(
      dashboardId: String
  ): Action[Map[String, VisualizationData]] =
    authActionBuilder.async(parse.json[Map[String, VisualizationData]]) {
      implicit request: AuthenticatedRequest[Map[String, VisualizationData]] =>
        dashboardService
          .updateChartsVisualizationData(dashboardId, request.body)(
            request.user
          )
          .map {
            case Some(dashboard) => Ok(Json.toJson(dashboard))
            case None            => NotFound
          }
          .recover {
            case _: AccessForbiddenException =>
              Forbidden
            case t =>
              log.error("Unexpected error while adding chart", t)
              InternalServerError
          }
    }
}
