package example.http4s.app.service.user.domain

case class LoginRequest(username: String, password: String)

case class SignupRequest(username: String,
                         firstName: String,
                         lastName: String,
                         email: String,
                         password: String,
                         role: Role)
