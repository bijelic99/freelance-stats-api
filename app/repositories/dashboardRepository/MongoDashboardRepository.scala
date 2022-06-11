package repositories.dashboardRepository
import model.{Chart, Dashboard}
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.play.json.compat.bson2json._
import reactivemongo.play.json.compat.json2bson._

class MongoDashboardRepository @Inject() (
    reactiveMongoApi: ReactiveMongoApi
)(implicit
    executionContext: ExecutionContext
) extends DashboardRepository {
  import utils.PlayJsonFormats._

  def dashboardsCollection: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection("dashboards"))

  override def get(id: String): Future[Option[Dashboard]] =
    dashboardsCollection.flatMap(_.find(Json.obj("id" -> id)).one[Dashboard])

  override def add(dashboard: Dashboard): Future[Dashboard] =
    dashboardsCollection
      .flatMap(_.insert.one(dashboard))
      .map {
        case result if result.writeErrors.isEmpty =>
          dashboard
        case result =>
          throw new Exception(
            s"Unexpected errors while adding dashboard to the database: '${result.writeErrors.map(_.errmsg).mkString(",\n")}'"
          )
      }

  override def addChart(dashboardId: String, chart: Chart): Future[Dashboard] =
    dashboardsCollection
      .flatMap(
        _.findAndUpdate(
          Json.obj("id" -> dashboardId),
          Json.obj(
            "$push" -> Json.obj(
              "charts" -> chart
            )
          ),
          fetchNewObject = true,
          upsert = false
        )
      )
      .map(_.value.map(_.asOpt[Dashboard].get).get)

  override def removeChart(
      dashboardId: String,
      chartId: String
  ): Future[Dashboard] =
    dashboardsCollection
      .flatMap(
        _.findAndUpdate(
          Json.obj("id" -> dashboardId),
          Json.obj(
            "pull" -> Json.obj(
              "charts" -> Json.obj("id" -> chartId)
            )
          ),
          fetchNewObject = true,
          upsert = false
        )
      )
      .map(_.value.map(_.asOpt[Dashboard].get).get)

  override def update(dashboard: Dashboard): Future[Dashboard] =
    dashboardsCollection
      .flatMap(
        _.findAndUpdate(
          Json.obj("id" -> dashboard.id),
          Json.obj("$set" -> dashboard),
          fetchNewObject = true,
          upsert = false
        )
      )
      .map(_.value.map(_.asOpt[Dashboard].get).get)

  override def delete(dashboardId: String): Future[Unit] =
    dashboardsCollection
      .flatMap(
        _.findAndUpdate(
          Json.obj("id" -> dashboardId),
          Json.obj("$set" -> Json.obj("deleted" -> true)),
          fetchNewObject = false,
          upsert = false
        )
      )
      .map(_ => ())
}
