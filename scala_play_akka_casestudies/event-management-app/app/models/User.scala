package models

import play.api.libs.json._
import slick.jdbc.MySQLProfile.api._

case class User(
    id:Option[Long],
    name:String,
    email:String,
    password:String
)

object User {
    implicit val userFormat: OFormat[User] = Json.format[User]
}

case class LoginRequest(
    email: String,
    password: String
)

object LoginRequest {
    implicit val loginRequestFormat: OFormat[LoginRequest] = Json.format[LoginRequest]
}

class UserTable(tag: Tag) extends Table[User](tag, "users"){
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def email = column[String]("email")
    def password = column[String]("password")

    override def * = (id.?, name, email, password) <> (User.tupled, User.unapply)
}
