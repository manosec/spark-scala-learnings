package controllers

import javax.inject.Inject
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import model.{ReceptionistEmployee, PremiseDeptRole, ReceptionistRegistration}
import security.JwtUtil
import middleware.ReceptionistGateway
import repositories.ReceptionistRepository

class ReceptionistController @Inject()(
    cc: ControllerComponents,
    receptionistAction: ReceptionistGateway,
    receptionistRepository: ReceptionistRepository
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

    def login: Action[JsValue] = Action(parse.json).async { request =>
        val email = (request.body \ "email").as[String]
        val password = (request.body \ "password").as[String]
        
        receptionistRepository.findByEmailAndPassword(email, password).map {
            case Some(receptionist) =>
                val token = JwtUtil.generateToken(
                    receptionist.id.toString, 
                    PremiseDeptRole.RECEPTIONIST
                )
                Ok(Json.obj("token" -> token))
            case None =>
                Unauthorized(Json.obj("message" -> "Invalid email or password"))
        }
    }

    def register: Action[JsValue] = Action(parse.json).async { request =>
        request.body.validate[ReceptionistRegistration] match {
            case JsSuccess(registration, _) =>
                receptionistRepository.findByEmail(registration.email).flatMap {
                    case Some(_) => 
                        Future.successful(BadRequest(Json.obj("message" -> "Email already exists")))
                    case None =>
                        val newReceptionist = ReceptionistEmployee.fromRegistration(registration)
                        receptionistRepository.create(newReceptionist).map { created =>
                            val token = JwtUtil.generateToken(
                                created.id.toString,
                                PremiseDeptRole.RECEPTIONIST
                            )
                            Created(Json.obj(
                                "message" -> "Registration successful",
                                "token" -> token
                            ))
                        }
                }
            case JsError(errors) =>
                Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
        }
    }
}