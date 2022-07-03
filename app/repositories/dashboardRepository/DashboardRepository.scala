package repositories.dashboardRepository

import com.google.inject.ImplementedBy
import model.{Chart, Dashboard}

import scala.concurrent.Future

@ImplementedBy(classOf[MongoDashboardRepository])
trait DashboardRepository {
  def get(id: String): Future[Option[Dashboard]]
  def add(dashboard: Dashboard): Future[Dashboard]
  def update(dashboard: Dashboard): Future[Option[Dashboard]]
  def delete(dashboardId: String): Future[Boolean]
  def getChart(dashboardId: String, chartId: String): Future[Option[Chart]]
  def addChart(dashboardId: String, chart: Chart): Future[Option[Chart]]
  def updateChart(dashboardId: String, chart: Chart): Future[Option[Chart]]
  def removeChart(dashboardId: String, chartId: String): Future[Boolean]
}
