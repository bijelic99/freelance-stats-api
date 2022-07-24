package services.chartServices

import com.sksamuel.elastic4s.requests.script.Script
import com.sksamuel.elastic4s.requests.searches.aggs.pipeline.BucketScriptPipelineAgg
import com.sksamuel.elastic4s.requests.searches.aggs.responses.bucket.{
  DateHistogram,
  DateHistogramBucket
}
import com.sksamuel.elastic4s.requests.searches.aggs.{
  AbstractAggregation,
  AvgAggregation,
  DateHistogramAggregation,
  HistogramOrder
}
import com.sksamuel.elastic4s.requests.searches.term.TermQuery
import com.sksamuel.elastic4s.requests.searches.{
  DateHistogramInterval,
  SearchRequest,
  SearchResponse
}
import com.sksamuel.elastic4s.{ElasticClient, RequestFailure, RequestSuccess}
import configuration.ElasticConfiguration
import model._
import org.slf4j.{Logger, LoggerFactory}
import utils.JobsQuery

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LineChartDataService @Inject() (
    client: ElasticClient,
    elasticConfiguration: ElasticConfiguration
)(implicit ec: ExecutionContext)
    extends ChartDataService[LineChart] {
  import com.sksamuel.elastic4s.ElasticDsl._

  private val log: Logger =
    LoggerFactory.getLogger(classOf[LineChartDataService])

  private val budgetAggregation
      : PartialFunction[LineChart, Seq[AbstractAggregation]] = {
    case chart
        if Seq(
          LineChart.FixedPriceJobValueInTime,
          LineChart.HourlyJobValueInTime
        ).contains(chart.dataType) =>
      Seq(
        AvgAggregation(
          name = "minimumAverage",
          field = Some("payment.budget.minimumUsd"),
          missing = None
        ),
        AvgAggregation(
          name = "maximumAverage",
          field = Some("payment.budget.minimumUsd"),
          missing = None
        ),
        BucketScriptPipelineAgg(
          name = "averageAverage",
          script = Script(
            lang = Some("painless"),
            script = "(params.minimumAverage + params.maximumAverage) / 2"
          ),
          bucketsPaths = Map(
            "minimumAverage" -> "minimumAverage.value",
            "maximumAverage" -> "maximumAverage.value"
          )
        )
      )
  }

  private def aggregation(chart: LineChart): AbstractAggregation =
    DateHistogramAggregation(
      name = chart.dataType.productPrefix,
      calendarInterval = Some(
        DateHistogramInterval.fromString(
          chart.interval.productPrefix.toLowerCase
        )
      ),
      order = Some(HistogramOrder.KEY_ASC),
      format = Some(chart.interval.preferableFormat),
      field = Some("created"),
      subaggs = budgetAggregation.lift(chart).getOrElse(Nil)
    )

  private def query(chart: LineChart) =
    bool(
      mustQueries = JobsQuery.mustQueries(chart) ++ Seq(
        chart.dataType match {
          case dataType
              if Seq(
                LineChart.FixedPriceJobValueInTime,
                LineChart.NumberOfFixedPriceJobsInTime
              ).contains(dataType) =>
            Some(
              TermQuery(
                field = "payment._type",
                value =
                  "com.freelanceStats.commons.models.indexedJob.FixedPrice"
              )
            )
          case dataType
              if Seq(
                LineChart.HourlyJobValueInTime,
                LineChart.NumberOfHourlyJobsInTime
              ).contains(dataType) =>
            Some(
              TermQuery(
                field = "payment._type",
                value = "com.freelanceStats.commons.models.indexedJob.Hourly"
              )
            )
          case _ =>
            None
        }
      ).flatten,
      shouldQueries = Nil,
      notQueries = Nil
    )

  private def searchRequest(chart: LineChart): SearchRequest =
    search(elasticConfiguration.index)
      .size(0)
      .query(query(chart))
      .aggs(aggregation(chart))

  private val parseResponseBucketForBudgetAggregations
      : PartialFunction[(LineChart.DataType, DateHistogramBucket), Seq[
        (String, Double)
      ]] = {
    case (dataType, bucket)
        if Seq(
          LineChart.FixedPriceJobValueInTime,
          LineChart.HourlyJobValueInTime
        ).contains(dataType) =>
      Seq(
        bucket.avg("minimumAverage"),
        bucket.avg("maximumAverage"),
        bucket.avg("averageAverage")
      ).map { avg =>
        avg.name -> avg.valueOpt.getOrElse(0)
      }
  }

  private val parseResponseBucketForCountAggregations
      : PartialFunction[(LineChart.DataType, DateHistogramBucket), Seq[
        (String, Double)
      ]] = {
    case (dataType, bucket)
        if Seq(
          LineChart.NumberOfJobsInTime,
          LineChart.NumberOfHourlyJobsInTime,
          LineChart.NumberOfFixedPriceJobsInTime
        ).contains(dataType) =>
      Seq("documentCount" -> bucket.docCount.toDouble)
  }

  private def parseSearchResponse(
      chart: LineChart,
      response: SearchResponse
  ): ChartData = {
    val data = response.aggs
      .result[DateHistogram](chart.dataType.productPrefix)
      .buckets
      .flatMap { bucket =>
        parseResponseBucketForBudgetAggregations
          .orElse(parseResponseBucketForCountAggregations)(
            chart.dataType,
            bucket
          )
          .map(bucket.date -> _)
      }
      .groupMap(_._2._1) { case (key, (_, value)) =>
        KeyValuePair(
          key,
          value
        )
      }
      .map { case (name, values) =>
        KeyValueMatrixRow(name, values)
      }
      .toSeq
    KeyValueMatrixData(
      chart.id,
      data
    )
  }

  override def getData(chart: LineChart): Future[ChartData] =
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
