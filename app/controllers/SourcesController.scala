package controllers

import configuration.SourcesConfiguration
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._

@Singleton
class SourcesController @Inject() (
    val controllerComponents: ControllerComponents,
    sourcesConfiguration: SourcesConfiguration
) extends BaseController {
  import utils.PlayJsonFormats._

  def get(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok(
        Json.toJson(sourcesConfiguration.sources)
      )
  }
}
