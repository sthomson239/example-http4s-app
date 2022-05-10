package example.http4s.app.service.post.domain

sealed trait PostValidationError extends Product with Serializable
case object EmptyPostError extends PostValidationError
case object PostNotFound extends PostValidationError

