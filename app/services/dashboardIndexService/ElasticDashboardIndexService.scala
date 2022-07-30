package services.dashboardIndexService

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink}
import com.sksamuel.elastic4s.requests.searches.queries.Query
import com.sksamuel.elastic4s.{ElasticClient, RequestFailure, RequestSuccess}
import configuration.ElasticConfiguration
import model.{Dashboard, DashboardMetadata, SearchResponse}
import org.slf4j.{Logger, LoggerFactory}
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
  import com.sksamuel.elastic4s.ElasticDsl._
  import utils.PlayJsonFormats._

  private val log: Logger =
    LoggerFactory.getLogger(classOf[ElasticDashboardIndexService])

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
      .pipe(DashboardMetadata(_, ""))
      .pipe(indexDashboard)

  override def reindexDashboards: Future[Unit] =
    dashboardRepository.getAll
      .map(DashboardMetadata(_, ""))
      .map(metadata =>
        updateById(elasticConfiguration.dashboardIndex, metadata.id)
          .docAsUpsert(
            Json.toJson(metadata).toString()
          )
      )
      .grouped(elasticConfiguration.dashboardReindexBatchSize)
      .mapAsync(1)(requests => client.execute(bulk(requests)))
      .toMat(
        Sink.foreach {
          case RequestSuccess(_, _, _, result) if !result.hasFailures =>
            log.trace("Batch indexed successfully")
          case RequestSuccess(_, _, _, result) =>
            val failures =
              result.failures.flatMap(_.error).map(_.reason).mkString(",\n")
            val message = s"Error while indexing batch, reasons: $failures"
            log.error(message)
            throw new Exception(message)
          case RequestFailure(_, _, _, error) =>
            throw new Exception("Error while indexing batch", error.asException)
        }
      )(Keep.right)
      .run()
      .map(_ => ())

  private def searchQuery(term: Option[String], userId: Option[String]): Query =
    boolQuery()
      .filter(
        boolQuery()
          .pipe(query =>
            userId
              .fold(query)(userId =>
                query.should(
                  termQuery("ownerId", userId),
                  termQuery("usersWithAccess", userId),
                  termQuery("public", true)
                )
              )
              .minimumShouldMatch(1)
          ),
        termQuery("deleted", false)
      )
      .pipe(query =>
        term
          .fold(query) { term =>
            query
              .should(
                matchQuery("name", term),
                matchQuery("chartNames", term),
                matchQuery("ownerUsername", term),
                termQuery("chartSources", term)
              )
              .minimumShouldMatch(1)
          }
      )

  override def searchDashboards(
      term: Option[String],
      userId: Option[String],
      size: Int,
      from: Int
  ): Future[SearchResponse[DashboardMetadata]] =
    client
      .execute(
        search(elasticConfiguration.dashboardIndex)
          .query(searchQuery(term, userId))
          .size(size)
          .from(from)
      )
      .map {
        case RequestSuccess(_, _, _, result) =>
          SearchResponse(
            result.hits.hits.toSeq
              .map(hit => Json.parse(hit.sourceAsString).as[DashboardMetadata]),
            result.totalHits
          )
        case RequestFailure(_, _, _, error) =>
          throw new Exception(
            "Error while searching for data",
            error.asException
          )
      }
}
