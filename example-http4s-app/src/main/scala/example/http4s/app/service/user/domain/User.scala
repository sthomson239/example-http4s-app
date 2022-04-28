package example.http4s.app.service.user.domain

import cats.Applicative
import tsec.authorization.AuthorizationInfo

case class User(
               username: String,
               firstName: String,
               lastName: String,
               email: String,
               passwordHash: String,
               role: Role,
               id: Option[Long] = None
               )

object User {
  implicit def authRole[F[_]](implicit F: Applicative[F]): AuthorizationInfo[F, Role, User] =
    (u: User) => F.pure(u.role)
}
