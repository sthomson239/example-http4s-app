package example.http4s.app.service.user.domain

import cats.data._
import cats.effect.Bracket
import cats.syntax.all._
import doobie._
import doobie.implicits._
import example.http4s.app.service.user.domain.User

object UserSQL {

  def insert(user: User): Update0 = sql"""
    INSERT INTO USERS (USERNAME, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD_HASH)
    VALUES (${user.username}, ${user.firstName}, ${user.lastName}, ${user.email}, ${user.passwordHash})
  """.update

  def select(userId: Long): Query0[User] = sql"""
    SELECT ID, USERNAME, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD_HASH
    FROM USERS
    WHERE ID = $userId
  """.query

  def delete(userId: Long): Update0 = sql"""
    DELETE FROM USERS WHERE ID = $userId
  """.update

  def selectByUsername(username: String): Query0[User] = sql"""
    SELECT ID, USERNAME, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD_HASH
    FROM USERS
    WHERE USERNAME = $username
  """.query
}

class UserRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F]) {
  import UserSQL._

  def create(user: User): F[User] =
    insert(user).withUniqueGeneratedKeys[Long](columns = "ID").map(id => user.copy(id = id.some)).transact(xa)

  def get(userId: Long): F[Option[User]] =
    select(userId: Long).option.transact(xa)

  def delete(userId: Long): F[Option[User]] =
    OptionT(get(userId)).semiflatMap(user => UserSQL.delete(userId).run.transact(xa).as(user)).value

  def findByUsername(username: String): F[Option[User]] =
    selectByUsername(username).option.transact(xa)
}

object UserRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](xa: Transactor[F]): UserRepositoryInterpreter[F] = {
    new UserRepositoryInterpreter(xa)
  }

}
