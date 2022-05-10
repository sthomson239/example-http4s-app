package example.http4s.app.service.follow.domain

case class Follow(id: Option[Long] = None, followerId: Long, followedId: Long)
