package example.http4s.app.service.follow.domain

import cats.data.EitherT

trait FollowValidationAlgebra[F[_]] {

  def followDoesNotExist(follow: Follow): EitherT[F, AlreadyFollowing.type, Unit]

  def exists(userId: Long): EitherT[F, UserNotFound.type, Unit]

}
