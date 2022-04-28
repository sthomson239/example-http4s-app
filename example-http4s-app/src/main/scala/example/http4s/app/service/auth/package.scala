package example.http4s.app.service

import example.http4s.app.service.user.domain.User
import org.http4s.Response
import tsec.authentication.{AugmentedJWT, SecuredRequest, TSecAuthService}

package object auth {
  type AuthService[F[_], Auth] = TSecAuthService[User, AugmentedJWT[Auth, Long], F]
  type AuthEndpoint[F[_], Auth] =
    PartialFunction[SecuredRequest[F, User, AugmentedJWT[Auth, Long]], F[Response[F]]]

}
