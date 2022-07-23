package services.chartServices

import com.sksamuel.elastic4s.requests.script.Script
import com.sksamuel.elastic4s.requests.searches.aggs.pipeline.BucketScriptPipelineAgg
import com.sksamuel.elastic4s.requests.searches.aggs.responses.bucket.Terms
import com.sksamuel.elastic4s.requests.searches.aggs.{
  AbstractAggregation,
  DateHistogramAggregation,
  TermsAggregation
}
import com.sksamuel.elastic4s.requests.searches.{
  DateHistogramInterval,
  SearchRequest,
  SearchResponse
}
import com.sksamuel.elastic4s.{ElasticClient, RequestFailure, RequestSuccess}
import configuration.ElasticConfiguration
import model.BubbleChart.{
  AverageNumberOfJobsPerDayPerMonth,
  AverageNumberOfJobsPerHourPerDay,
  AverageNumberOfJobsPerMonthPerYear
}
import model.{BubbleChart, ChartData, KeyValuePair, KeyValueSeqData}
import org.slf4j.{Logger, LoggerFactory}
import utils.JobsQuery

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BubbleChartDataService @Inject() (
    client: ElasticClient,
    elasticConfiguration: ElasticConfiguration
)(implicit ec: ExecutionContext)
    extends ChartDataService[BubbleChart] {
  import com.sksamuel.elastic4s.ElasticDsl._

  private val log: Logger =
    LoggerFactory.getLogger(classOf[BubbleChartDataService])

  private val averageNumberOfJobsPerHourPerDayAggregation
      : PartialFunction[BubbleChart, AbstractAggregation] = {
    case BubbleChart(id, _, _, _, _, _, t @ AverageNumberOfJobsPerHourPerDay) =>
      TermsAggregation(id)
        .script(
          Script(
            script = "doc['created'].value.getHour()",
            lang = Some("painless")
          )
        )
        .size(24)
        .addSubagg(
          DateHistogramAggregation(
            name = "number_of_days",
            field = Some("created"),
            calendarInterval = Some(DateHistogramInterval.Week)
          )
        )
        .addSubagg(
          BucketScriptPipelineAgg(
            name = t.productPrefix,
            bucketsPaths = Map(
              "doc_count" -> "_count",
              "number_of_days" -> "number_of_days._bucket_count"
            ),
            script = Script(
              script = "params.doc_count / params.number_of_days",
              lang = Some("painless")
            )
          )
        )
  }

  private val averageNumberOfJobsPerDayPerMonthAggregation
      : PartialFunction[BubbleChart, AbstractAggregation] = {
    case BubbleChart(
          id,
          _,
          _,
          _,
          _,
          _,
          t @ AverageNumberOfJobsPerDayPerMonth
        ) =>
      TermsAggregation(id)
        .script(
          Script(
            script = "doc['created'].value.getDayOfMonth()",
            lang = Some("painless")
          )
        )
        .size(31)
        .addSubagg(
          DateHistogramAggregation(
            name = "number_of_months",
            field = Some("created"),
            calendarInterval = Some(DateHistogramInterval.Month)
          )
        )
        .addSubagg(
          BucketScriptPipelineAgg(
            name = t.productPrefix,
            bucketsPaths = Map(
              "doc_count" -> "_count",
              "number_of_months" -> "number_of_months._bucket_count"
            ),
            script = Script(
              script = "params.doc_count / params.number_of_months",
              lang = Some("painless")
            )
          )
        )
  }

  private val averageNumberOfJobsPerMonthPerYearAggregation
      : PartialFunction[BubbleChart, AbstractAggregation] = {
    case BubbleChart(
          id,
          _,
          _,
          _,
          _,
          _,
          t @ AverageNumberOfJobsPerMonthPerYear
        ) =>
      TermsAggregation(id)
        .script(
          Script(
            script = "doc['created'].value.getMonthValue()",
            lang = Some("painless")
          )
        )
        .size(12)
        .addSubagg(
          DateHistogramAggregation(
            name = "number_of_years",
            field = Some("created"),
            calendarInterval = Some(DateHistogramInterval.Year)
          )
        )
        .addSubagg(
          BucketScriptPipelineAgg(
            name = t.productPrefix,
            bucketsPaths = Map(
              "doc_count" -> "_count",
              "number_of_years" -> "number_of_years._bucket_count"
            ),
            script = Script(
              script = "params.doc_count / params.number_of_years",
              lang = Some("painless")
            )
          )
        )
  }

  private def aggregation(chart: BubbleChart): AbstractAggregation =
    averageNumberOfJobsPerHourPerDayAggregation
      .orElse(averageNumberOfJobsPerDayPerMonthAggregation)
      .orElse(averageNumberOfJobsPerMonthPerYearAggregation)(chart)

  private def searchRequest(chart: BubbleChart): SearchRequest =
    search(elasticConfiguration.index)
      .size(0)
      .query(JobsQuery(chart))
      .aggs(aggregation(chart))

  private def parseSearchResponse(
      chart: BubbleChart,
      response: SearchResponse
  ): ChartData = {
    val data = response.aggs.result[Terms](chart.id).buckets.map { bucket =>
      val average = bucket
        .getAgg(chart.dataType.productPrefix)
        .get
        .dataAsMap("value")
        .asInstanceOf[Double]
      KeyValuePair(bucket.key, average)
    }
    KeyValueSeqData(chartId = chart.id, data = data)
  }

  override def getData(chart: BubbleChart): Future[ChartData] =
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
