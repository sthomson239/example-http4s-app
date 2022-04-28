package example.http4s.app.service.follow.domain

import cats.Functor
import cats.data._
import cats.Monad
import cats.syntax.all._
import example.http4s.app.service.post.domain.Post
import example.http4s.app.service.user.domain.UserValidationAlgebra

class FollowService[F[_]](repository: FollowRepositoryAlgebra[F], validation: FollowValidationAlgebra[F]) {

  def create(follow: Follow)(implicit M: Monad[F]): EitherT[F, FollowValidationError, Follow] =
    for {
      _ <- validation.exists(follow.followerId) >> validation.exists(follow.followedId)
      _ <- validation.followDoesNotExist(follow)
      createdFollow <- EitherT.liftF(repository.create(follow))
    } yield createdFollow

  def unfollow(followerId: Long, followedId: Long)(implicit F: Functor[F]): F[Unit] =
    repository.delete(followerId: Long, followedId: Long).as(())

  def allFollowedPosts(userId: Long): F[List[Post]] =
    repository.getPosts(userId)

}

object FollowService {
  def apply[F[_]](repository: FollowRepositoryAlgebra[F], validation: FollowValidationAlgebra[F]): FollowService[F] =
    new FollowService[F](repository, validation)
}
