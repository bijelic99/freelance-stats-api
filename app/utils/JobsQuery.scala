package utils

import com.sksamuel.elastic4s.ElasticDsl.{bool, rangeQuery, termQuery}
import com.sksamuel.elastic4s.requests.searches.queries.Query
import model.Chart

object JobsQuery {
  def apply(chart: Chart): Query =
    bool(
      mustQueries = Seq(
        chart.dateFrom.map(df => rangeQuery("created").gt(df.toString)),
        chart.dateTo.map(dt => rangeQuery("created").lt(dt.toString)),
        chart.source.map(termQuery("source", _))
      ).flatten,
      shouldQueries = Nil,
      notQueries = Nil
    )
}
