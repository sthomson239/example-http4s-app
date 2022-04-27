package example.http4s.app.service.follow.domain

import example.http4s.app.service.post.domain.Post

trait FollowRepositoryAlgebra[F[_]] {

  def create(follow: Follow): F[Follow]

  def delete(followerId: Long, followedId: Long): F[Option[Follow]]

  def getPosts(userId: Long): F[List[Post]]

}
