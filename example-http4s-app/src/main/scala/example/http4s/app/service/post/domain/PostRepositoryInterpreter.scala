package example.http4s.app.service.post.domain

import cats.data._
import cats.effect.Bracket
import cats.syntax.all._
import doobie._
import doobie.implicits._
import example.http4s.app.service.user.domain.User

import java.time.ZonedDateTime

object PostSQL {

  def insert(post: Post): Update0 = sql"""
    INSERT INTO POSTS (AUTHOR_ID, CREATED_AT, CONTENT)
    VALUES (${post.authorId}, ${post.createdAt}, ${post.content})
  """.update

  def select(postId: Long): Query0[Post] = sql"""
    SELECT ID, AUTHOR_ID, CREATED_AT, CONTENT
    FROM POSTS
    WHERE ID = $postId
  """.query

  def delete(userId: Long): Update0 = sql"""
    DELETE FROM POSTS WHERE ID = $userId
  """.update

  def selectByCreatedAt(minDate: ZonedDateTime): Query0[Post] = sql"""
    SELECT ID, AUTHOR_ID, CREATED_AT, CONTENT
    FROM POSTS
    WHERE CREATED_AT >= $minDate
  """.query
}

class PostRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F]) extends PostRepositoryAlgebra[F] {
  import PostSQL._

  def create(post: Post): F[Post] =
    insert(post).withUniqueGeneratedKeys[Long]("ID").map(id => post.copy(postId = id.some)).transact(xa)

  def get(postId: Long): F[Option[Post]] =
    select(postId).option.transact(xa)

  def delete(postId: Long): F[Option[Post]] =
    OptionT(get(postId)).semiflatMap(post => PostSQL.delete(postId).run.transact(xa).as(post)).value

  def findByCreatedAt(minDate: ZonedDateTime): F[List[Post]] =
    selectByCreatedAt(minDate).to[List].transact(xa)
}

object PostRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](xa: Transactor[F]): PostRepositoryInterpreter[F] =
    new PostRepositoryInterpreter(xa)
}

