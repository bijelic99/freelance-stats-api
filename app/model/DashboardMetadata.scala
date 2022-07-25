package model

import org.joda.time.DateTime

case class DashboardMetadata(
    id: String,
    ownerId: String,
    usersWithAccess: Seq[String],
    name: String,
    chartNames: Seq[String],
    chartSources: Seq[String],
    deleted: Boolean,
    createdAt: DateTime = DateTime.now(),
    modifiedAt: DateTime = DateTime.now(),
    public: Boolean = false
)

object DashboardMetadata {
  def apply(dashboard: Dashboard): DashboardMetadata =
    DashboardMetadata.apply(
      id = dashboard.id,
      ownerId = dashboard.ownerId,
      usersWithAccess = dashboard.usersWithAccess,
      name = dashboard.name,
      chartNames = dashboard.charts.map(_.name),
      chartSources = dashboard.charts.flatMap(_.source).distinct,
      deleted = dashboard.deleted,
      createdAt = dashboard.createdAt,
      modifiedAt = dashboard.modifiedAt,
      public = dashboard.public
    )
}
