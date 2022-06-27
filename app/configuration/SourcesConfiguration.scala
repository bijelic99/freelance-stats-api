package configuration

import model.Source
import play.api.Configuration

import javax.inject.Inject

class SourcesConfiguration @Inject() (configuration: Configuration) {
  val sources: Seq[Source] =
    configuration
      .getOptional[Seq[Configuration]]("sources")
      .getOrElse(Nil)
      .map(Source(_))
}
