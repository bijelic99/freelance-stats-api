package model

case class VisualizationLimits(
    chartClass: Class[_ <: Chart[_]],
    minW: Int,
    minH: Int,
    maxW: Int,
    maxH: Int
)
