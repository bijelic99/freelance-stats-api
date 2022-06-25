package services.chartServices
import com.sksamuel.elastic4s.{ElasticClient, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.requests.searches.{SearchRequest, SearchResponse}
import com.sksamuel.elastic4s.requests.searches.aggs.AbstractAggregation
import com.sksamuel.elastic4s.requests.searches.aggs.responses.bucket.Terms
import com.sksamuel.elastic4s.requests.searches.queries.Query
import configuration.ElasticConfiguration
import model.PieChart.{Category, Language, Timezone, WorkType}
import model.{ChartData, PieChart, PieData}
import org.slf4j.{Logger, LoggerFactory}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PieChartService @Inject() (
    client: ElasticClient,
    elasticConfiguration: ElasticConfiguration
)(implicit ec: ExecutionContext)
    extends ChartService[PieChart] {
  import com.sksamuel.elastic4s.ElasticDsl._

  private val log: Logger = LoggerFactory.getLogger(classOf[PieChartService])

  private def query(chart: PieChart): Query =
    bool(
      mustQueries = Seq(
        chart.dateFrom.map(df => rangeQuery("created").gt(df.toString)),
        chart.dateTo.map(dt => rangeQuery("created").lt(dt.toString)),
        chart.source.map(termQuery("source", _))
      ).flatten,
      shouldQueries = Nil,
      notQueries = Nil
    )

  private def aggregation(chart: PieChart): AbstractAggregation = chart match {
    case PieChart(id, _, _, _, _, _, _, WorkType) =>
      termsAgg(id, "positionType")
    case PieChart(id, _, _, _, _, _, _, Category) =>
      nestedAggregation(id, "categories")
        .subAggregations(
          filterAgg(s"$id-filter-agg", termQuery("categories.topLevel", true))
            .subAggregations(
              termsAgg(s"$id-terms-agg", "categories.name")
            )
        )
    case PieChart(id, _, _, _, _, _, _, Language) =>
      termsAgg(id, "language.names")
    case PieChart(id, _, _, _, _, _, _, Timezone) =>
      termsAgg(id, "employer.timezone.name")
  }

  private def searchRequest(chart: PieChart): SearchRequest =
    search(elasticConfiguration.index)
      .size(0)
      .query(query(chart))
      .aggs(aggregation(chart))

  private val parseResponsePF
      : PartialFunction[(PieChart, SearchResponse), ChartData] = {
    case (PieChart(id, _, _, _, _, _, _, dataType), response)
        if Seq(WorkType, Language, Timezone).contains(dataType) =>
      val data = response.aggs
        .result[Terms](id)
        .buckets
        .map(bucket => (bucket.key, bucket.docCount.toDouble))
        .toMap
      PieData(id, data)
  }

  private val parseCategoryResponsePF
      : PartialFunction[(PieChart, SearchResponse), ChartData] = {
    case (PieChart(id, _, _, _, _, _, _, Category), response) =>
      val data = response.aggs
        .nested(id)
        .filter(s"$id-filter-agg")
        .result[Terms](s"$id-terms-agg")
        .buckets
        .map(bucket => (bucket.key, bucket.docCount.toDouble))
        .toMap
      PieData(id, data)
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
