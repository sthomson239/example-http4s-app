package example.http4s.app.service.post.domain

import cats.data.EitherT

trait PostValidationAlgebra[F[_]] {
  def notEmpty(post: Post): EitherT[F, EmptyPostError.type, Unit]

  def exists(id: Option[Long]): EitherT[F, PostNotFound.type, Unit]
}
