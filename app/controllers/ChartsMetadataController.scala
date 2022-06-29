package controllers

import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class ChartsMetadataController @Inject() (
    val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BaseController {

  def get(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok.sendResource(
        "charts-metadata.json"
      )
  }
}
