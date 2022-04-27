package example.http4s.app

import cats.effect._
import example.http4s.app.util.config._
import org.http4s.server.{Router, Server}
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object Server extends IOApp{

  private def server[F[_]: ContextShift: ConcurrentEffect: Timer]: Resource[F, Server[F]] = ???

  def run(args: List[String]): IO[ExitCode] = server.use(_ => IO.never).as(ExitCode.Success)

}
