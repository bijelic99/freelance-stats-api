package controllers

import com.freelanceStats.jwtAuth.actions.JwtAuthActionBuilder
import com.freelanceStats.jwtAuth.models.AuthenticatedRequest
import configuration.SourcesConfiguration
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._

@Singleton
class SourcesController @Inject() (
    val controllerComponents: ControllerComponents,
    sourcesConfiguration: SourcesConfiguration,
    authActionBuilder: JwtAuthActionBuilder
) extends BaseController {
  import utils.PlayJsonFormats._

  def get(): Action[AnyContent] = authActionBuilder {
    implicit request: AuthenticatedRequest[AnyContent] =>
      Ok(
        Json.toJson(sourcesConfiguration.sources)
      )
  }
}
