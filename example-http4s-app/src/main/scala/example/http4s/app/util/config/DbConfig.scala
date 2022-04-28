package example.http4s.app.util.config

import cats.syntax.functor._
import cats.effect.{Async, Blocker, ContextShift, Resource, Sync}
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext

case class DatabaseConnectionsConfig(poolSize: Int)
case class DbConfig(url: String,
                    driver: String,
                    user: String,
                    password: String,
                    connections: DatabaseConnectionsConfig)

object DbConfig {
  def dbTransactor[F[_]: Async: ContextShift](
                                               dbc: DbConfig,
                                               connEc: ExecutionContext,
                                               blocker: Blocker,
                                             ): Resource[F, HikariTransactor[F]] =
    HikariTransactor
      .newHikariTransactor[F](dbc.driver, dbc.url, dbc.user, dbc.password, connEc, blocker)

  /**
   * Runs the flyway migrations against the target database
   */
  def initializeDb[F[_]](cfg: DbConfig)(implicit S: Sync[F]): F[Unit] =
    S.delay {
      val fw: Flyway =
        Flyway
          .configure()
          .dataSource(cfg.url, cfg.user, cfg.password)
          .load()
      fw.migrate()
    }.as(())
}