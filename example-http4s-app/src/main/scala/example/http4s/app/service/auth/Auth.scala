package example.http4s.app.service.auth

import cats.MonadError
import cats.effect._
import example.http4s.app.service.user.domain.{Role, User}
import org.http4s.Response
import tsec.authentication.{AugmentedJWT, BackingStore, IdentityStore, JWTAuthenticator, SecuredRequest, TSecAuthService}
import tsec.authorization.BasicRBAC
import tsec.common.SecureRandomId
import tsec.jws.mac.JWSMacCV
import tsec.jwt.algorithms.JWTMacAlgo
import tsec.mac.jca.MacSigningKey

import scala.concurrent.duration._

object Auth {
  def jwtAuth[F[_]: Sync, Auth: JWTMacAlgo](
                                           key: MacSigningKey[Auth],
                                           authRepo: BackingStore[F, SecureRandomId, AugmentedJWT[Auth, Long]],
                                           userRepo: IdentityStore[F, Long, User],
                                           )(implicit cv: JWSMacCV[F, Auth]): JWTAuthenticator[F, Long, User, Auth] =
    JWTAuthenticator.backed.inBearerToken(
      expiryDuration = 1.hour,
      maxIdle = None,
      tokenStore = authRepo,
      identityStore = userRepo,
      signingKey = key,
    )

  def allRoles[F[_], Auth](
                            pf: PartialFunction[SecuredRequest[F, User, AugmentedJWT[Auth, Long]], F[Response[F]]],
                          )(implicit F: MonadError[F, Throwable]): TSecAuthService[User, AugmentedJWT[Auth, Long], F] =
    TSecAuthService.withAuthorization(BasicRBAC.all[F, Role, User, Auth][F, AugmentedJWT[Auth, Long]])(pf)


  def adminOnly[F[_], Auth](
                             pf: PartialFunction[SecuredRequest[F, User, AugmentedJWT[Auth, Long]], F[Response[F]]],
                           )(implicit F: MonadError[F, Throwable]): TSecAuthService[User, AugmentedJWT[Auth, Long], F] =
    TSecAuthService.withAuthorization(BasicRBAC[F, Role, User, Auth](Role.Admin)[F, AugmentedJWT[Auth, Long]])(pf)



}
