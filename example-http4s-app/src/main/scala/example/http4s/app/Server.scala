package example.http4s.app

import cats.effect._
import example.http4s.app.util.config._
import org.http4s.server.{Router, Server}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import example.http4s.app.service.follow.domain.{FollowRepositoryInterpreter, FollowService, FollowValidationInterpreter}
import example.http4s.app.service.post.domain.{PostRepositoryInterpreter, PostService, PostValidationInterpreter}
import example.http4s.app.service.user.domain.UserRepositoryInterpreter
import io.circe.config.parser
import io.circe.generic.auto.exportDecoder
import tsec.authentication.SecuredRequestHandler
import tsec.mac.jca.HMACSHA256

object Server extends IOApp{

  private def server[F[_]: ContextShift: ConcurrentEffect: Timer]: Resource[F, Server[F]] = {
    for {
      conf <- Resource.eval(parser.decodePathF[F, Config]("example"))
      serverEc <- ExecutionContexts.cachedThreadPool[F]
      connEc <- ExecutionContexts.fixedThreadPool[F](conf.dbConfig.connections.poolSize)
      txnEc <- ExecutionContexts.cachedThreadPool[F]
      xa <- DbConfig.dbTransactor(conf.dbConfig, connEc, Blocker.liftExecutionContext(txnEc))
      key <- Resource.eval(HMACSHA256.generateKey[F])
      userRepo = UserRepositoryInterpreter[F](xa)
      postRepo = PostRepositoryInterpreter[F](xa)
      followRepo = FollowRepositoryInterpreter[F](xa)
      userValidation = UserValidationInterpreter[F](userRepo)
      postValidation = PostValidationInterpreter[F](postRepo)
      followValidation = FollowValidationInterpreter[F](followRepo, userRepo)
      userService = UserService[F](userRepo)
      postService = PostService[F](postRepo, postValidation)
      followService = FollowService[F](followRepo, followValidation)
      routeAuth = SecuredRequestHandler(authenticator)
      httpApp = Router(
        "/users" -> UserEndpoints
          .endpoints[F, BCrypt, HMACSHA256](userService, BCrypt.syncPasswordHasher[F], routeAuth),
        "/pets" -> PetEndpoints.endpoints[F, HMACSHA256](petService, routeAuth),
        "/orders" -> OrderEndpoints.endpoints[F, HMACSHA256](orderService, routeAuth),
      ).orNotFound
      _ <- Resource.eval(DbConfig.initializeDb(conf.dbConfig))
      server <- BlazeServerBuilder[F](serverEc)
        .bindHttp(conf.serverConfig.port, conf.serverConfig.host)
        .withHttpApp(httpApp)
        .resource
    } yield server
  }

  def run(args: List[String]): IO[ExitCode] = server.use(_ => IO.never).as(ExitCode.Success)

}
