package example.http4s.app.service.user.domain

import cats.data.EitherT

trait UserValidationAlgebra[F[_]] {

  def doesNotExist(user: User): EitherT[F, UserAlreadyExists.type, Unit]

  def exists(userId: Long): EitherT[F, UserNotFound.type, Unit]

}
