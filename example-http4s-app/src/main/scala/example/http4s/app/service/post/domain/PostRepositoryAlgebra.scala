package example.http4s.app.service.post.domain

import java.time.ZonedDateTime

trait PostRepositoryAlgebra[F[_]] {

  def create(post: Post): F[Post]

  def get(postId: Long): F[Option[Post]]

  def delete(orderId: Long): F[Option[Post]]

  def findByCreatedAt(minDate: ZonedDateTime): F[List[Post]]

}
