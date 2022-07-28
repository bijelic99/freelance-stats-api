package model

case class SearchResponse[T](hits: Seq[T], total: Long)
