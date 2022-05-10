package example.http4s.app.service.user.domain

import cats.data._
import cats.Functor
import cats.Monad
import cats.syntax.functor._

class UserService[F[_]](repository: UserRepositoryAlgebra[F], validation: UserValidationAlgebra[F]) {

  def createUser(user: User)(implicit M: Monad[F]): EitherT[F, UserAlreadyExists.type, User] =
    for {
      _ <- validation.doesNotExist(user)
      newUser <- EitherT.liftF(repository.create(user))
    } yield newUser

  def getUser(userId: Long)(implicit F: Functor[F]): EitherT[F, UserNotFound.type, User] = EitherT {
    repository.get(userId).toRight(UserNotFound)
  }

  def deleteUser(userId: Long)(implicit F: Functor[F]): F[Unit] =
    repository.delete(userId).as(())

  def findByUsername(username: String)(implicit F: Functor[F]): EitherT[F, UserNotFound.type, User] = EitherT {
    repository.findByUsername(username).map {
      case Some(user) =>
        Right(user)
      case _ =>
        Left(UserNotFound)
    }
  }
}

object UserService {
  def apply[F[_]](repository: UserRepositoryAlgebra[F], validation: UserValidationAlgebra[F]): UserService[F] =
    new UserService[F](repository, validation)
}
