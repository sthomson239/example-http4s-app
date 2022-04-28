package example.http4s.app.service.follow.api

import cats.data.EitherT
import cats.effect.Sync
import cats.syntax.all._
import example.http4s.app.service.auth.{Auth, AuthEndpoint}
import example.http4s.app.service.follow.domain.{Follow, FollowService}
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
import example.http4s.app.service.follow.domain

class FollowEndpoints[F[_]: Sync, A, Auth: JWTMacAlgo] extends Http4sDsl[F] {
  implicit val followDecoder: EntityDecoder[F, Follow] = jsonOf

  def newFollowEndpoint(followService: FollowService[F]): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root asAuthed user =>
      val action = for {
        follow <- req.request.as[Follow]
        saved <- followService.create(follow).value
      } yield saved

      action.flatMap {
        case Right(saved) =>
          Ok(saved.asJson)
        case Left(_) =>
          Conflict(s"Post cannot be empty")
      }
  }

  def unfollowEndpoint(followService: FollowService[F]): AuthEndpoint[F, Auth] = {
    case DELETE -> Root / LongVar(id) asAuthed user =>
      for {
        _ <- followService.unfollow(user.id.get, id)
        resp <- Ok()
      } yield resp
  }

  def getFollowedPosts(followService: FollowService[F]): AuthEndpoint[F, Auth] = {
    case GET -> Root / "followed-posts" asAuthed user =>
      for {
        posts <- followService.allFollowedPosts(user.id.get)
        resp = Ok(posts.asJson)
      } yield resp
  }

  def allEndpoints(followService: FollowService[F], auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]): HttpRoutes[F]=  {
    auth.liftService(newFollowEndpoint(followService)
    .orElse(unfollowEndpoint(followService)
    .orElse(getFollowedPosts(followService))))
  }
}
