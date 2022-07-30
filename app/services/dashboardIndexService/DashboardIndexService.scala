package services.dashboardIndexService

import com.google.inject.ImplementedBy
import model.{Dashboard, DashboardMetadata, SearchResponse}

import scala.concurrent.Future

@ImplementedBy(classOf[ElasticDashboardIndexService])
trait DashboardIndexService {
  def indexDashboard(
      dashboardMetadata: DashboardMetadata
  ): Future[DashboardMetadata]
  def indexDashboard(dashboard: Dashboard): Future[DashboardMetadata]
  def reindexDashboards: Future[Unit]
  def searchDashboards(
      term: Option[String],
      userId: Option[String],
      size: Int,
      from: Int
  ): Future[SearchResponse[DashboardMetadata]]
}
