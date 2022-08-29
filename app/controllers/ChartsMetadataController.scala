package controllers

import com.freelanceStats.jwtAuth.actions.JwtAuthActionBuilder
import com.freelanceStats.jwtAuth.models.AuthenticatedRequest
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class ChartsMetadataController @Inject() (
    val controllerComponents: ControllerComponents,
    authActionBuilder: JwtAuthActionBuilder
)(implicit ec: ExecutionContext)
    extends BaseController {

  def get(): Action[AnyContent] = authActionBuilder {
    implicit request: AuthenticatedRequest[AnyContent] =>
      Ok.sendResource(
        "charts-metadata.json"
      )
  }
}
