package controllers

import configuration.VisualizationLimitsConfiguration
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._

@Singleton
class VisualizationLimitsController @Inject()(val controllerComponents: ControllerComponents, visualizationLimitsConfiguration: VisualizationLimitsConfiguration)
    extends BaseController {
  import util.PlayJsonFormats._

  def get(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(
      Json.toJson(visualizationLimitsConfiguration.limits)
    )
  }
}
