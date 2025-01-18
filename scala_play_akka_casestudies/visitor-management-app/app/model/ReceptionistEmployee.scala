package model

import play.api.libs.json._
import slick.jdbc.MySQLProfile.api._

// Case class for registration requests
case class ReceptionistRegistration(
    email: String,
    password: String
)

object ReceptionistRegistration {
    implicit val format: OFormat[ReceptionistRegistration] = Json.format[ReceptionistRegistration]
}

case class ReceptionistEmployee(
    id: Int,
    email: String,
    password: String,
)

object ReceptionistEmployee {
    implicit val format: OFormat[ReceptionistEmployee] = Json.format[ReceptionistEmployee]
    
    def fromRegistration(registration: ReceptionistRegistration): ReceptionistEmployee = 
        ReceptionistEmployee(0, registration.email, registration.password)
}

class ReceptionistEmployeeTable(tag: Tag) extends Table[ReceptionistEmployee](tag, "receptionist_employee") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def email = column[String]("email")
    def password = column[String]("password")

    def * = (id, email, password) <> (ReceptionistEmployee.tupled, ReceptionistEmployee.unapply)
}