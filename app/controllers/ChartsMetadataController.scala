package controllers

import configuration.{ChartMetadataConfiguration}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._

@Singleton
class ChartsMetadataController @Inject() (
    val controllerComponents: ControllerComponents,
    chartMetadataConfiguration: ChartMetadataConfiguration
) extends BaseController {
  import utils.PlayJsonFormats._

  def get(): Action[AnyContent] = Action {
    implicit request: Request[AnyContent] =>
      Ok(
        Json.toJson(chartMetadataConfiguration.chartsMetadata)
      )
  }
}
