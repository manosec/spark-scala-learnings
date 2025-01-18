package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import repositories.UserRepository
import models.User
import scala.concurrent.{ExecutionContext, Future}
import jwtTokenGeneration.JwtUtil
import models.{Event, EventStatus}
import repositories.EventRepository
import java.time.LocalDateTime
import middleware.AuthAction

@Singleton
class EventController @Inject()(
    val controllerComponents: ControllerComponents,
    eventRepository: EventRepository,
    authAction: AuthAction
)(implicit ec: ExecutionContext) extends BaseController {

    def createEvent() = authAction.async(parse.json) { request =>
        request.body.validate[Event].fold(
            errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
            event => eventRepository.create(event)
                .map(createdEvent => Ok(Json.toJson(createdEvent)))
                .recover {
                    case _: IllegalArgumentException => BadRequest(Json.obj("message" -> "Invalid event data"))
                    case _: java.sql.SQLIntegrityConstraintViolationException => 
                        Conflict(Json.obj("message" -> "Event already exists"))
                    case ex: Exception => 
                        InternalServerError(Json.obj("message" -> s"Error creating event: ${ex.getMessage}"))
                }
        )
    }

    def getEventById(eventId: Long) = authAction.async { request =>
        eventRepository.getEventById(eventId).flatMap {
            case Some(event) => Future.successful(Ok(Json.toJson(event)))
            case None => Future.successful(NotFound(Json.obj("message" -> "Event not found")))
        }.recover {
            case ex: Exception => 
                InternalServerError(Json.obj("message" -> s"Error retrieving event: ${ex.getMessage}"))
        }
    }   

    def updateEvent(eventId: Long) = authAction.async(parse.json) { request =>  
        request.body.validate[Event].fold(
            errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
            event => eventRepository.update(eventId, event)
                .map(updatedEvent => Ok(Json.toJson(updatedEvent)))
                .recover {
                    case _: RuntimeException => NotFound(Json.obj("message" -> "Event not found"))
                    case ex: Exception => InternalServerError(Json.obj("message" -> "Error updating event"))
                }
        )
    }

    def deleteEvent(eventId: Long) = authAction.async { request =>
        eventRepository.delete(eventId)
            .map(_ => NoContent)
            .recover {
                case ex: Exception => InternalServerError(Json.obj("message" -> "Error deleting event"))
            }
    }

    def listEvents(status: Option[String] = None) = authAction.async { request =>
        val eventStatus = status.map(s => EventStatus.withName(s))
        
        eventRepository.listEvents(status = eventStatus)
            .map(events => Ok(Json.toJson(events)))
            .recover {
                case ex: Exception => InternalServerError(Json.obj("message" -> "Error listing events"))
            }
    }
}