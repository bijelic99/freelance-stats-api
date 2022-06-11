package repositories.dashboardRepository

import com.google.inject.ImplementedBy
import model.{Chart, Dashboard}

import scala.concurrent.Future

@ImplementedBy(classOf[MongoDashboardRepository])
trait DashboardRepository {
  def get(id: String): Future[Option[Dashboard]]
  def add(dashboard: Dashboard): Future[Dashboard]
  def addChart(dashboardId: String, chart: Chart): Future[Dashboard]
  def removeChart(dashboardId: String, chartId: String): Future[Dashboard]
  def update(dashboard: Dashboard): Future[Dashboard]
  def delete(dashboardId: String): Future[Unit]
}
