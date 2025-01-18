package models

import slick.jdbc.MySQLProfile.api._
import play.api.libs.json._

case class Team(id: Long = 0L, 
               teamName: String, 
               teamType: TeamType.Value, 
               teamEmail: String, 
               teamDescription: String) {
  def toCreateRequest: CreateTeamRequest = CreateTeamRequest(
    teamName = teamName,
    teamType = teamType,
    teamEmail = teamEmail,
    teamDescription = teamDescription
  )
}

object TeamType extends Enumeration {
  type TeamType = Value
  val CATERING, CLEANING, SECURITY, ENTERTAINMENT, TRANSPORTATION, MEDICAL, OTHER = Value
  
  // Add JSON format for TeamType
  implicit val format: Format[TeamType] = Format(
    Reads[TeamType](js => js.validate[String].map(s => TeamType.withName(s))),
    Writes[TeamType](v => JsString(v.toString))
  )

  // Existing Slick mapper
  implicit val teamTypeMapper: BaseColumnType[TeamType.Value] = MappedColumnType.base[TeamType.Value, String](
    e => e.toString,
    s => TeamType.withName(s)
  )
}

class TeamTable(tag: Tag) extends Table[Team](tag, "teams") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def teamName = column[String]("team_name")
  def teamType = column[TeamType.Value]("team_type")
  def teamEmail = column[String]("team_email")
  def teamDescription = column[String]("team_description")

  def * = (id, teamName, teamType, teamEmail, teamDescription) <> ((Team.apply _).tupled, Team.unapply)
}

// Move Team object before CreateTeamRequest
object Team {
  // Add JSON format for Team model
  implicit val format: Format[Team] = Json.format[Team]
}

// Create a separate case class for team creation
case class CreateTeamRequest(
    teamName: String, 
    teamType: TeamType.Value, 
    teamEmail: String, 
    teamDescription: String
)

object CreateTeamRequest {
    implicit val format: Format[CreateTeamRequest] = Json.format[CreateTeamRequest]
}