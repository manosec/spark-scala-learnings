package controllers

import javax.inject.Inject
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import repositories.PremiseDeptRepository
import model.{PremiseDept, PremiseDeptRole}
import middleware.ReceptionistGateway

class PremiseDeptController @Inject()(
    cc: ControllerComponents,
    premiseDeptRepository: PremiseDeptRepository,
    receptionistAction: ReceptionistGateway
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

    def getAllPremiseDepts: Action[AnyContent] = receptionistAction.async {
        premiseDeptRepository.findAll().map(depts => Ok(Json.toJson(depts)))
    }

    def getPremiseDeptById(id: Int): Action[AnyContent] = receptionistAction.async {
        premiseDeptRepository.findById(id).map {
            case Some(dept) => Ok(Json.toJson(dept))
            case None => NotFound
        }
    }

    def createPremiseDept: Action[JsValue] = receptionistAction(parse.json).async { request =>
        println(s"Received JSON: ${request.body}")
        
        request.body.validate[PremiseDept](PremiseDept.createFormat) match {
            case JsSuccess(premiseDept, _) =>
                premiseDeptRepository.create(premiseDept).map(created => 
                    Ok(Json.toJson(created))
                ).recover {
                    case e: Exception => 
                        BadRequest(Json.obj(
                            "error" -> "Failed to create department",
                            "details" -> e.getMessage
                        ))
                }
            case JsError(errors) =>
                Future.successful(BadRequest(Json.obj(
                    "error" -> "Validation failed",
                    "details" -> JsError.toJson(errors)
                )))
        }
    }

    def updatePremiseDept(id: Int): Action[JsValue] = receptionistAction(parse.json).async { request =>
        println(s"Update request for id $id with body: ${request.body}")
        
        request.body.validate[PremiseDept](PremiseDept.format) match {
            case JsSuccess(premiseDept, _) =>
                // Ensure we use the ID from the path
                val deptToUpdate = premiseDept.copy(id = id)
                println(s"Attempting to update department: $deptToUpdate")
                
                premiseDeptRepository.update(deptToUpdate).flatMap {
                    case 1 => 
                        premiseDeptRepository.findById(id).map {
                            case Some(updated) => 
                                println(s"Successfully updated to: $updated")
                                Ok(Json.toJson(updated))
                            case None => 
                                println(s"Department not found after update")
                                NotFound(Json.obj("error" -> s"Department with id $id not found after update"))
                        }
                    case 0 => 
                        Future.successful(NotFound(Json.obj("error" -> s"Update failed for department with id $id")))
                    case n => 
                        Future.successful(InternalServerError(Json.obj("error" -> s"Unexpected update count: $n")))
                }.recover {
                    case e: Exception => 
                        println(s"Update failed with error: ${e.getMessage}")
                        BadRequest(Json.obj(
                            "error" -> "Update failed",
                            "details" -> e.getMessage
                        ))
                }
            case JsError(errors) =>
                Future.successful(BadRequest(Json.obj(
                    "error" -> "Validation failed",
                    "details" -> JsError.toJson(errors)
                )))
        }
    }

    def deletePremiseDept(id: Int): Action[AnyContent] = receptionistAction.async {
        premiseDeptRepository.delete(id).map(_ => NoContent)
    }
}