package controllers

import model.Dashboard
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json
import play.api.mvc._
import repositories.dashboardRepository.DashboardRepository

import java.util.UUID
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.chaining._

class DashboardController @Inject() (
    val controllerComponents: ControllerComponents,
    dashboardRepository: DashboardRepository
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
        .map(dashboard => Ok(Json.toJson(dashboard)))
        .recover { t =>
          log.error("Unexpected error while adding dashboard", t)
          InternalServerError
        }
  }

  def put(id: String): Action[Dashboard] = Action.async(parse.json[Dashboard]) {
    implicit request: Request[Dashboard] =>
      request.body
        .pipe {
          case dashboard if dashboard.id.equals(id) =>
            dashboardRepository
              .update(dashboard)
              .map(dashboard => Ok(Json.toJson(dashboard)))
              .recover { t =>
                log.error("Unexpected error while editing dashboard", t)
                InternalServerError
              }
          case _ =>
            Future.successful(
              BadRequest("Updating the id is not allowed")
            )
        }

  }

  def delete(id: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      dashboardRepository
        .delete(id)
        .map(_ => Ok)
        .recover { t =>
          log.error("Unexpected error while deleting dashboard", t)
          InternalServerError
        }
  }
}
