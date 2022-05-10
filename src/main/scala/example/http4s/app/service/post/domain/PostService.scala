package example.http4s.app.service.post.domain

import cats.Functor
import cats.data._
import cats.Monad
import cats.syntax.all._

class PostService[F[_]](repository: PostRepositoryAlgebra[F], validation: PostValidationAlgebra[F]) {

  def create(post: Post)(implicit M: Monad[F]): EitherT[F, EmptyPostError.type, Post] =
    for {
      _ <- validation.notEmpty(post)
      createdPost <- EitherT.liftF(repository.create(post))
    } yield createdPost

  def get(id: Long)(implicit F: Functor[F]): EitherT[F, PostNotFound.type, Post] =
    EitherT.fromOptionF(repository.get(id), PostNotFound)

  def delete(id: Long)(implicit F: Functor[F]): F[Unit] =
    repository.delete(id).as(())
}

object PostService {
  def apply[F[_]](repository: PostRepositoryAlgebra[F], validation: PostValidationAlgebra[F]): PostService[F] =
    new PostService[F](repository, validation)
}
