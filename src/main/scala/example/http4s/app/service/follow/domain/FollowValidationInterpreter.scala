package example.http4s.app.service.follow.domain

import cats.Applicative
import cats.data.{EitherT, OptionT}
import cats.syntax.all._
import example.http4s.app.service.user.domain.{UserAlreadyExists, UserRepositoryAlgebra}

class FollowValidationInterpreter[F[_]: Applicative](followRepository: FollowRepositoryAlgebra[F], userRepository: UserRepositoryAlgebra[F]) extends FollowValidationAlgebra[F] {

  def followDoesNotExist(follow: Follow): EitherT[F, AlreadyFollowing.type, Unit] = OptionT {
    followRepository.get(follow.followerId, follow.followedId)
  }.map(_ => AlreadyFollowing).toLeft(())

  def exists(userId: Long): EitherT[F, UserNotFound.type, Unit] =
    OptionT{userRepository.get(userId)}.toRight(UserNotFound).as(())
}

object FollowValidationInterpreter {
  def apply[F[_]: Applicative](followRepository: FollowRepositoryAlgebra[F], userRepository: UserRepositoryAlgebra[F]) =
    new FollowValidationInterpreter[F](followRepository, userRepository)
}
