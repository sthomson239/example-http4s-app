package example.http4s.app.service.post.api

import cats.data.EitherT
import cats.effect.Sync
import cats.syntax.all._
import example.http4s.app.service.auth
import example.http4s.app.service.auth.{Auth, AuthEndpoint}
import example.http4s.app.service.post.domain.{EmptyPostError, Post, PostNotFound, PostService}
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

class PostEndpoints[F[_]: Sync, A, Auth: JWTMacAlgo] extends Http4sDsl[F]  {

  implicit val postDecoder: EntityDecoder[F, Post] = jsonOf[F, Post]

  def newPostEndpoint(postService: PostService[F]): AuthEndpoint[F, Auth] = {
    case req @ POST -> Root asAuthed user =>
      val action = for {
        post <- req.request.as[Post].map(_.copy(authorId = user.id))
        saved <- postService.create(post).value
      } yield saved

      action.flatMap {
        case Right(saved) =>
          Ok(saved.asJson)
        case Left(EmptyPostError) =>
          Conflict(s"Post cannot be empty")
      }
  }

  def getPostEndpoint(postService: PostService[F]): AuthEndpoint[F, Auth] = {
    case GET -> Root / LongVar(id) asAuthed _ =>
      postService.get(id).value.flatMap {
        case Right(found) => Ok(found.asJson)
        case Left(PostNotFound) => NotFound("Post was not found")
      }
  }

  def deletePostEndpoint(postService: PostService[F]): AuthEndpoint[F, Auth] = {
    case DELETE -> Root / LongVar(id) asAuthed _ =>
      for {
        _ <- postService.delete(id)
        resp <- Ok()
      } yield resp
  }

  def allEndpoints(postService: PostService[F], auth: SecuredRequestHandler[F, Long, User, AugmentedJWT[Auth, Long]]): HttpRoutes[F] = {
    auth.liftService(newPostEndpoint(postService)
      .orElse(getPostEndpoint(postService))
      .orElse(deletePostEndpoint(postService)))
  }

}
