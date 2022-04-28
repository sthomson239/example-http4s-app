package example.http4s.app.service.user.api

import cats.data.EitherT
import cats.effect.Sync
import cats.syntax.all._
import example.http4s.app.service.auth.Auth
import example.http4s.app.service.user.domain.{LoginRequest, SignupRequest, User, UserAlreadyExists, UserAuthFailed, UserNotFound, UserService}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import tsec.common.Verified
import tsec.jwt.algorithms.JWTMacAlgo
import tsec.passwordhashers.{PasswordHash, PasswordHasher}
import tsec.authentication._

class UserEndpoints[F[_]: Sync, A, Auth: JWTMacAlgo] extends Http4sDsl[F] {

  implicit val userDecoder: EntityDecoder[F, User] = jsonOf
  implicit val loginReqDecoder: EntityDecoder[F, LoginRequest] = jsonOf

  implicit val signupReqDecoder: EntityDecoder[F, SignupRequest] = jsonOf

  def loginEndpoint(userService: UserService[F], cryptService: PasswordHasher[F, A], auth: Authenticator[F, Long, User, AugmentedJWT[Auth, Long]]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / "login" =>
        val action = for {
          login: LoginRequest <- EitherT.liftF(req.as[LoginRequest])
          username = login.username
          user: User <- userService.findByUsername(username).leftMap(_ => UserAuthFailed)
          check <- EitherT.liftF(cryptService.checkpw(login.password, PasswordHash[A](user.passwordHash)))
          _ <-
            if (check == Verified) EitherT.rightT[F, UserAuthFailed.type](())
            else EitherT.leftT[F, User](UserAuthFailed)
          token <- user.id match {
            case None => throw new Exception("Invalid")
            case Some(id) => EitherT.right[UserAuthFailed.type](auth.create(id))
          }
        } yield (user, token)
        action.value.flatMap {
          case Right((user, token)) => Ok(user.asJson).map(auth.embed(_, token))
          case Left(UserAuthFailed) =>
            BadRequest(s"Auth failed for user")
        }
    }

  def signupEndpoint(userService: UserService[F], crypt: PasswordHasher[F, A]): HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req @ POST -> Root =>
        val action = for {
          signup <- req.as[SignupRequest]
          passwordHash <- crypt.hashpw(signup.password)
          user <- User(signup.username, signup.firstName, signup.lastName, signup.email, passwordHash, signup.role).pure[F]
          result <- userService.createUser(user).value
        } yield result
        action.flatMap {
          case Right(saved) => Ok(saved.asJson)
          case Left(UserAlreadyExists) => NotFound("User already exists")
        }
    }
  }

  def allEndpoints(userService: UserService[F], cryptService: PasswordHasher[F, A], auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]): HttpRoutes[F] = {
    loginEndpoint(userService, cryptService, auth.authenticator) <+> signupEndpoint(userService, cryptService)
  }
}

object UserEndpoints {
  def endpoints[F[_]: Sync, A, Auth: JWTMacAlgo](
                                                  userService: UserService[F],
                                                  cryptService: PasswordHasher[F, A],
                                                  auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]],
                                                ): HttpRoutes[F] =
    new UserEndpoints[F, A, Auth].allEndpoints(userService, cryptService, auth)
