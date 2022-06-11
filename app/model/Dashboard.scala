package model

import org.joda.time.DateTime

case class Dashboard(
    id: String,
    ownerId: String,
    usersWithAccess: Seq[String],
    name: String,
    charts: Seq[Chart],
    deleted: Boolean,
    createdAt: DateTime = DateTime.now(),
    modifiedAt: DateTime = DateTime.now(),
    public: Boolean = false
)
