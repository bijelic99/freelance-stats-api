package configuration

import play.api.Configuration

import javax.inject.Inject

class ElasticConfiguration @Inject() (
    configuration: Configuration
) {

  val endpoint: String = configuration.get[String]("elastic.endpoint")

  val jobIndex: String = configuration.get[String]("elastic.jobIndex")

  val dashboardIndex: String =
    configuration.get[String]("elastic.dashboardIndex")

  val dashboardReindexBatchSize: Int = configuration
    .getOptional[Int]("elastic.dashboardReindexBatchSize")
    .getOrElse(10)

}
