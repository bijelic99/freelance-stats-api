package services.dashboardIndexService

import com.google.inject.ImplementedBy
import model.{Dashboard, DashboardMetadata}

import scala.concurrent.Future

@ImplementedBy(classOf[ElasticDashboardIndexService])
trait DashboardIndexService {
  def indexDashboard(
      dashboardMetadata: DashboardMetadata
  ): Future[DashboardMetadata]
  def indexDashboard(dashboard: Dashboard): Future[DashboardMetadata]
  def reindexDashboards: Future[Unit]
  def searchDashboards(
      term: String,
      offset: Long
  ): Future[Seq[DashboardMetadata]]
}
