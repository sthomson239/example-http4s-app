package example.http4s.app.service.post.domain

import java.time.ZonedDateTime

case class Post(
               postId: Option[Long] = None,
               authorId: Option[Long] = None,
               createdAt: Option[ZonedDateTime] = None,
               content: String,
               tags: List[String]
               )
