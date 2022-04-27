package example.http4s.app.service.post.domain

import cats.{Applicative, Functor, Monad}
import cats.data._
import cats.syntax.all._

class PostValidationInterpreter[F[_]: Applicative](repository: PostRepositoryAlgebra[F]) extends PostValidationAlgebra[F] {

  def notEmpty(post: Post): EitherT[F, EmptyPostError.type, Unit] = EitherT {
    if (post.content.isEmpty) {
      Either.right[EmptyPostError.type, Unit](()).pure[F]
    } else {
      Either.left[EmptyPostError.type, Unit](EmptyPostError).pure[F]
    }
  }

  def exists(id: Option[Long]): EitherT[F, PostNotFound.type, Unit] = EitherT {
    id match {
      case Some(postId) =>
        repository.get(postId).map {
          case Some(_) => Right(())
          case _ => Left(PostNotFound)
        }
      case _ =>
        Either.left[PostNotFound.type, Unit](PostNotFound).pure[F]
    }
  }
}

object PostValidationInterpreter {
  def apply[F[_]: Applicative](repository: PostRepositoryAlgebra[F]) =
    new PostValidationInterpreter[F](repository)
}