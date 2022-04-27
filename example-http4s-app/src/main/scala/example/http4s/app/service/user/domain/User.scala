package example.http4s.app.service.user.domain

case class User(
               username: String,
               firstName: String,
               lastName: String,
               email: String,
               passwordHash: String,
               id: Option[Long] = None
               )
