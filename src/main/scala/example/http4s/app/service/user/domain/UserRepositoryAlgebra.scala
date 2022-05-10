package example.http4s.app.service.user.domain

import cats.data.OptionT

trait UserRepositoryAlgebra[F[_]] {

  def create(user: User): F[User]

  def get(userId: Long): OptionT[F, User]

  def delete(userId: Long): F[Option[User]]

  def findByUsername(username: String): F[Option[User]]

}
