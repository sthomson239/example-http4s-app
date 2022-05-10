package example.http4s.app.service.follow.domain

import cats.data._
import cats.effect.Bracket
import cats.syntax.all._
import doobie._
import doobie.implicits._
import example.http4s.app.service.post.domain.Post
import example.http4s.app.service.user.domain.User

object FollowSQL {
  def insert(follow: Follow): Update0 = sql"""
    INSERT INTO FOLLOWS (FOLLOWER_ID, FOLLOWED_ID)
    VALUES (${follow.followerId}, ${follow.followedId})
  """.update

  def select(followId: Long): Query0[Follow] = sql"""
    SELECT ID, FOLLOWER_ID, FOLLOWED_ID
    FROM FOLLOWS
    WHERE id = $followId
  """.query

  def selectFollowId(followerId: Long, followedId: Long): Query0[Follow] = sql"""
    SELECT ID, FOLLOWER_ID, FOLLOWED_ID
    FROM FOLLOWS
    WHERE FOLLOWER_ID = $followerId AND FOLLOWED_ID = $followedId
  """.query

  def delete(followerId: Long, followedId: Long): Update0 = sql"""
    DELETE FROM FOLLOWS WHERE FOLLOWER_ID = $followerId AND FOLLOWED_ID = $followedId
  """.update

  def deleteAll(userId: Long): Update0 = sql"""
    DELETE FROM FOLLOWS WHERE FOLLOWER_ID = $userId
  """.update

  def selectPosts(userId: Long): Query0[Post] = sql"""
    SELECT ID, AUTHOR_ID, CREATED_AT, CONTENT
    FROM POSTS
    WHERE AUTHOR_ID IN (
        SELECT FOLLOWED_ID
        FROM FOLLOWS
        WHERE FOLLOWER_ID = $userId
    )
  """.query
}

class FollowRepositoryInterpreter[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F]) extends FollowRepositoryAlgebra[F] {
  import FollowSQL._

  def create(follow: Follow): F[Follow] =
    insert(follow).withUniqueGeneratedKeys[Long](columns="ID").map(id => follow.copy(id = id.some)).transact(xa)

  def delete(followerId: Long, followedId: Long): F[Option[Follow]] = {
    OptionT(selectFollowId(followerId, followedId).option.transact(xa)).semiflatMap(follow => FollowSQL.delete(followerId, followedId).run.transact(xa).as(follow)).value
  }

  def getPosts(userId: Long): F[List[Post]] =
    selectPosts(userId).to[List].transact(xa)

}

object FollowRepositoryInterpreter {
  def apply[F[_]: Bracket[*[_], Throwable]](xa: Transactor[F]): FollowRepositoryInterpreter[F] =
    new FollowRepositoryInterpreter(xa)
}
