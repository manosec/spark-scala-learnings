package controllers

import javax.inject.Inject
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import repositories.VisitorRepository
import model.{Visitor, VisitorRegistration, VisitorNotification}
import middleware.ReceptionistGateway
import services.KafkaProducerService

class VisitorController @Inject()(
    cc: ControllerComponents, 
    visitorRepository: VisitorRepository, 
    kafkaProducerService: KafkaProducerService,
    receptionistAction: ReceptionistGateway
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

    def getAllVisitors: Action[AnyContent] = receptionistAction.async {
        visitorRepository.findAll().map { visitors =>
            if (visitors.isEmpty) {
                NoContent
            } else {
                Ok(Json.toJson(visitors))
            }
        }.recover {
            case ex: Exception =>
                InternalServerError(Json.obj("error" -> "Failed to fetch visitors"))
        }
    }

    def getVisitorById(id: Int): Action[AnyContent] = receptionistAction.async {
        visitorRepository.findById(id).map {
            case Some(visitor) => Ok(Json.toJson(visitor))
            case None => NotFound
        }
    }   

    def createVisitor: Action[JsValue] = receptionistAction(parse.json).async { request =>
        request.body.validate[VisitorRegistration] match {
            case JsSuccess(registration, _) =>
                Visitor.fromRegistration(registration) match {
                    case Right(visitor) =>
                        visitorRepository.create(visitor).map { newVisitor => 
                            val notification = VisitorNotification(
                                visitorId = newVisitor.id,
                                visitorName = newVisitor.name,
                                visitorEmail = newVisitor.email,
                                notificationType = "VISITOR_CREATED"
                            )
                            kafkaProducerService.sendVisitorNotification(notification)
                            Created(Json.toJson(newVisitor))
                        }
                    case Left(error) =>
                        Future.successful(BadRequest(Json.obj(
                            "error" -> "Invalid date format",
                            "message" -> error,
                        )))
                }
            case JsError(errors) =>
                Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
        }
    }   
    
    def updateVisitor(id: Int): Action[JsValue] = receptionistAction(parse.json).async { request =>
        val visitor = request.body.as[Visitor]
        visitorRepository.update(visitor).map(_ => Ok)
    }

    def deleteVisitor(id: Int): Action[AnyContent] = receptionistAction.async {
        visitorRepository.delete(id).map(_ => NoContent)
    }
}