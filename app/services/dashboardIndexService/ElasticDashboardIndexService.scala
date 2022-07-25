package services.dashboardIndexService

import akka.actor.ActorSystem
import akka.stream.scaladsl.Keep
import com.sksamuel.elastic4s.{ElasticClient, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.akka.streams.{
  BatchElasticSink,
  RequestBuilder,
  SinkSettings
}
import configuration.ElasticConfiguration
import model.{Dashboard, DashboardMetadata}
import play.api.libs.json.Json
import repositories.dashboardRepository.DashboardRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.chaining._

class ElasticDashboardIndexService @Inject() (
    client: ElasticClient,
    elasticConfiguration: ElasticConfiguration,
    dashboardRepository: DashboardRepository
)(implicit ec: ExecutionContext, system: ActorSystem)
    extends DashboardIndexService {
  import utils.PlayJsonFormats._
  import com.sksamuel.elastic4s.ElasticDsl._

  override def indexDashboard(
      dashboardMetadata: DashboardMetadata
  ): Future[DashboardMetadata] =
    client
      .execute(
        updateById(
          elasticConfiguration.dashboardIndex,
          dashboardMetadata.id
        ).doc(
          Json.toJson(dashboardMetadata).toString()
        )
      )
      .map {
        case RequestSuccess(_, _, _, _) =>
          dashboardMetadata
        case RequestFailure(_, _, _, error) =>
          throw new Exception(
            s"Error while indexing dashboard with id of: '${dashboardMetadata.id}'",
            error.asException
          )
      }

  override def indexDashboard(dashboard: Dashboard): Future[DashboardMetadata] =
    dashboard
      .pipe(DashboardMetadata(_))
      .pipe(indexDashboard)

  implicit val requestBuilder: RequestBuilder[DashboardMetadata] =
    (t: DashboardMetadata) =>
      updateById(elasticConfiguration.dashboardIndex, t.id).doc(
        Json.toJson(t).toString()
      )

  override def reindexDashboards: Future[Unit] =
    dashboardRepository.getAll
      .map(DashboardMetadata(_))
      .grouped(elasticConfiguration.dashboardReindexBatchSize)
      .toMat(
        new BatchElasticSink[DashboardMetadata](
          client = client,
          settings = SinkSettings(refreshAfterOp = false)
        )
      )(Keep.left)
      .run()
      .map(_ => ())

  override def searchDashboards(
      term: String,
      offset: Long
  ): Future[Seq[DashboardMetadata]] = ???
}
