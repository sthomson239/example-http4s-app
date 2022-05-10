package example.http4s.app.service.user.domain

import cats.Eq
import tsec.authorization.{AuthGroup, SimpleAuthEnum}

case class Role(roleRepr: String)

object Role extends SimpleAuthEnum[Role, String] {
  val User: Role = Role("User")
  val Admin: Role = Role("User")

  override val values: AuthGroup[Role] = AuthGroup(User, Admin)

  override def getRepr(t: Role): String = t.roleRepr

  implicit val eq: Eq[Role] = Eq.fromUniversalEquals[Role]
}
