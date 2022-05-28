package model

case class Dashboard(
    id: String,
    userId: String,
    name: String,
    charts: Seq[Chart[_]],
    deleted: Boolean
)
