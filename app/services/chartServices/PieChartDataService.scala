package services.chartServices
import com.sksamuel.elastic4s.requests.searches.aggs.AbstractAggregation
import com.sksamuel.elastic4s.requests.searches.aggs.responses.bucket.Terms
import com.sksamuel.elastic4s.requests.searches.{SearchRequest, SearchResponse}
import com.sksamuel.elastic4s.{ElasticClient, RequestFailure, RequestSuccess}
import configuration.ElasticConfiguration
import model.PieChart.{Category, Language, Timezone, WorkType}
import model.{ChartData, KeyValuePair, KeyValueSeqData, PieChart}
import org.slf4j.{Logger, LoggerFactory}
import utils.JobsQuery

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PieChartDataService @Inject() (
    client: ElasticClient,
    elasticConfiguration: ElasticConfiguration
)(implicit ec: ExecutionContext)
    extends ChartDataService[PieChart] {
  import com.sksamuel.elastic4s.ElasticDsl._

  private val log: Logger =
    LoggerFactory.getLogger(classOf[PieChartDataService])

  private def aggregation(chart: PieChart): AbstractAggregation = chart match {
    case PieChart(id, _, _, _, _, _, WorkType) =>
      termsAgg(id, "positionType")
    case PieChart(id, _, _, _, _, _, Category) =>
      nestedAggregation(id, "categories")
        .subAggregations(
          filterAgg(s"$id-filter-agg", termQuery("categories.topLevel", true))
            .subAggregations(
              termsAgg(s"$id-terms-agg", "categories.name")
            )
        )
    case PieChart(id, _, _, _, _, _, Language) =>
      termsAgg(id, "language.names")
    case PieChart(id, _, _, _, _, _, Timezone) =>
      termsAgg(id, "employer.timezone.name")
  }

  private def searchRequest(chart: PieChart): SearchRequest =
    search(elasticConfiguration.jobIndex)
      .size(0)
      .query(JobsQuery(chart))
      .aggs(aggregation(chart))

  private val parseResponsePF
      : PartialFunction[(PieChart, SearchResponse), ChartData] = {
    case (PieChart(id, _, _, _, _, _, dataType), response)
        if Seq(WorkType, Language, Timezone).contains(dataType) =>
      val data = response.aggs
        .result[Terms](id)
        .buckets
        .map(bucket => KeyValuePair(bucket.key, bucket.docCount.toDouble))
      KeyValueSeqData(id, data)
  }

  private val parseCategoryResponsePF
      : PartialFunction[(PieChart, SearchResponse), ChartData] = {
    case (PieChart(id, _, _, _, _, _, Category), response) =>
      val data = response.aggs
        .nested(id)
        .filter(s"$id-filter-agg")
        .result[Terms](s"$id-terms-agg")
        .buckets
        .map(bucket => KeyValuePair(bucket.key, bucket.docCount.toDouble))
      KeyValueSeqData(id, data)
  }

  private def parseSearchResponse(
      chart: PieChart,
      response: SearchResponse
  ): ChartData =
    parseResponsePF
      .orElse(parseCategoryResponsePF)(chart -> response)

  override def getData(chart: PieChart): Future[ChartData] =
    client
      .execute(
        searchRequest(chart)
      )
      .map {
        case RequestSuccess(_, _, _, result) =>
          parseSearchResponse(chart, result)
        case RequestFailure(_, _, _, error) =>
          val message =
            s"Error while getting data for chart with id of: '${chart.id}'"
          log.error(message, error.asException)
          throw new Exception(message, error.asException)
      }

}
