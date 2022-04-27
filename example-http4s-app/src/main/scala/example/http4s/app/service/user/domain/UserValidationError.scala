package example.http4s.app.service.user.domain

sealed trait UserValidationError extends Product with Serializable
case object UserAlreadyExists extends UserValidationError
case object UserNotFound extends UserValidationError