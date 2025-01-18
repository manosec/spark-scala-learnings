package repositories

import javax.inject.{Inject, Singleton}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.MySQLProfile
import scala.concurrent.{ExecutionContext, Future}
import models.{Team, TeamTable, CreateTeamRequest}


@Singleton
class TeamRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                            (implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[MySQLProfile] {
  import profile.api._
  
  val teams = TableQuery[TeamTable]

  def create(request: CreateTeamRequest): Future[Long] = {
    val insertQuery = (teams.map(t => 
      (t.teamName, t.teamType, t.teamEmail, t.teamDescription)
    ) returning teams.map(_.id)) += (
      request.teamName,
      request.teamType,
      request.teamEmail,
      request.teamDescription
    )
    
    db.run(insertQuery)
  }   

  def getById(id: Long): Future[Option[Team]] = {
    val query = teams.filter(_.id === id)
    db.run(query.result.headOption).recover {
      case ex: Exception =>
        println(s"Error fetching team with id $id: ${ex.getMessage}")
        None
    }
  }   

  def update(id: Long, team: Team): Future[Team] = {
    println(team)
    db.run(teams.filter(_.id === id).update(team)).map(_ => team)
  }

  def delete(id: Long): Future[Int] = {
    db.run(teams.filter(_.id === id).delete)
  }

  def getAll: Future[Seq[Team]] = {
    db.run(teams.result)
  }

  def getByEventId(eventId: Long): Future[Seq[Team]] = {
    // Implement this based on your event-team relationship
    // This is a placeholder implementation
    db.run(teams.result)
  }
}