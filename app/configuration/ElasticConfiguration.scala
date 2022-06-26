package configuration

import play.api.Configuration

import javax.inject.Inject

class ElasticConfiguration @Inject() (
    configuration: Configuration
) {

  val endpoint: String = configuration.get[String]("elastic.endpoint")

  val index: String = configuration.get[String]("elastic.index")

}
