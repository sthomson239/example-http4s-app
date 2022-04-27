package example.http4s.app.service.user.domain

import cats.Applicative
import cats.data.{EitherT, OptionT}
import cats.syntax.all._

class UserValidationInterpreter[F[_]: Applicative](repository: UserRepositoryAlgebra[F]) {

  def doesNotExist(user: User): EitherT[F, UserAlreadyExists.type, Unit] = OptionT {
    repository.findByUsername(user.username)
  }.map(_ => UserAlreadyExists).toLeft(())

  def exists(userId: Long): EitherT[F, UserNotFound.type, Unit] =
    OptionT{repository.get(userId)}.toRight(UserNotFound).as(())

}
