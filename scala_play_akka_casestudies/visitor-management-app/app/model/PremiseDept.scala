package model

import play.api.libs.json._
import slick.jdbc.MySQLProfile.api._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._

object PremiseDeptRole extends Enumeration {
    type PremiseDeptRole = Value
    val IT, SECURITY, HOST_EMPLOYEE, RECEPTIONIST = Value
    
    implicit val format: Format[PremiseDeptRole] = new Format[PremiseDeptRole] {
        def reads(json: JsValue): JsResult[PremiseDeptRole] = 
            JsSuccess(PremiseDeptRole.withName(json.as[String]))
        def writes(role: PremiseDeptRole): JsValue = JsString(role.toString)
    }

    implicit val premiseDeptRoleColumnType: BaseColumnType[PremiseDeptRole] = 
        MappedColumnType.base[PremiseDeptRole, String](
            _.toString,
            PremiseDeptRole.withName
        )
}

case class PremiseDept(
    id: Int,
    name: String,
    email: String,
    mobile: String,
    role: PremiseDeptRole.PremiseDeptRole
)

object PremiseDept {
    implicit val format: OFormat[PremiseDept] = Json.format[PremiseDept]
    
    implicit val createFormat: Reads[PremiseDept] = (
        (__ \ "name").read[String] ~
        (__ \ "email").read[String] ~
        (__ \ "mobile").read[String] ~
        (__ \ "role").read[PremiseDeptRole.PremiseDeptRole]
    )(PremiseDept.apply(0, _, _, _, _))

    def create(name: String, email: String, mobile: String, role: PremiseDeptRole.PremiseDeptRole): PremiseDept = 
        PremiseDept(0, name, email, mobile, role)
}

class PremiseDeptTable(tag: Tag) extends Table[PremiseDept](tag, "premise_dept") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def email = column[String]("email")
    def mobile = column[String]("mobile")
    def role = column[PremiseDeptRole.PremiseDeptRole]("role")

    def * = (id, name, email, mobile, role) <> (
        PremiseDept.tupled,
        PremiseDept.unapply
    )
}