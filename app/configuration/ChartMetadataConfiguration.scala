package configuration

import model.ChartMetadata
import play.api.Configuration

import javax.inject.Inject

class ChartMetadataConfiguration @Inject() (
    configuration: Configuration
) {
  val chartsMetadata: Seq[ChartMetadata] =
    configuration
      .getOptional[Seq[Configuration]]("charts")
      .getOrElse(Nil)
      .map(ChartMetadata(_))
}
