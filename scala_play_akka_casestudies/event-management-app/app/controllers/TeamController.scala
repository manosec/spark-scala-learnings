package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.libs.json._
import models.Team
import models.TeamType
import repositories.TeamRepository
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import middleware.AuthAction
import models.CreateTeamRequest


@Singleton
class TeamController @Inject()(val controllerComponents: ControllerComponents, 
                             teamRepository: TeamRepository,
                             authAction: AuthAction)
                             (implicit ec: ExecutionContext) extends BaseController {

    implicit val teamTypeFormat: Format[TeamType.Value] = Json.formatEnum(TeamType)
    implicit val teamFormat: Format[Team] = Json.format[Team]
  
    def createTeam = authAction.async(parse.json) { request =>
        try {
            request.body.validate[CreateTeamRequest].fold(
                errors => {
                    Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
                },
                createRequest => {
                    val team = Team(
                        id = 0L, // This will be ignored by the database as it's auto-incremented
                        teamName = createRequest.teamName,
                        teamType = createRequest.teamType,
                        teamEmail = createRequest.teamEmail,
                        teamDescription = createRequest.teamDescription
                    )
                    teamRepository.create(team.toCreateRequest).map { createdTeam =>
                        Ok(Json.toJson(createdTeam))
                    }.recover {
                        case e => 
                            println(s"Error creating team: ${e.getMessage}")
                            InternalServerError(Json.obj("message" -> e.getMessage))
                    }
                }
            )
        } catch {
            case e: Exception =>
                println(s"Exception in createTeam: ${e.getMessage}")
                Future.successful(BadRequest(Json.obj("message" -> e.getMessage)))
        }
    }

    def getTeam(id: Long) = Action.async { implicit request =>
        teamRepository.getById(id).map {
            case Some(team) => Ok(Json.toJson(team))
            case None => NotFound(Json.obj(
                "message" -> s"Team with id $id not found",
                "status" -> "NOT_FOUND"
            ))
        }.recover {
            case e: Exception =>
                InternalServerError(Json.obj(
                    "message" -> "An error occurred while fetching the team",
                    "error" -> e.getMessage
                ))
        }
    }       

    def updateTeam(id: Long) = authAction.async(parse.json) { request =>
        val team = request.body.as[Team]
        teamRepository.update(id, team).map { updatedTeam =>
            
            Ok(Json.toJson(updatedTeam))
        }.recover {
            case e => InternalServerError(e.getMessage)
        }
    }

    def deleteTeam(id: Long) = authAction.async {
        teamRepository.delete(id).map { _ =>
            NoContent
        }.recover {
            case e => InternalServerError(e.getMessage)
        }
    }

    def getAllTeams = authAction.async {
        teamRepository.getAll.map { teams =>
            Ok(Json.toJson(teams))
        }
    }

    def getTeamByEventId(eventId: Long) = authAction.async {
        teamRepository.getByEventId(eventId).map { teams =>
            Ok(Json.toJson(teams))
        }
    }
}