package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import repositories.UserRepository
import models.User
import models.LoginRequest
import scala.concurrent.{ExecutionContext, Future}
import jwtTokenGeneration.JwtUtil

@Singleton
class UserController @Inject()(
    val controllerComponents: ControllerComponents,
    userRepository: UserRepository
)(implicit ec: ExecutionContext) extends BaseController {

    def register() = Action.async(parse.json) { request =>
        request.body.validate[User].fold(
            errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
            user => userRepository.findByEmail(user.email).flatMap {
                case Some(_) => 
                    Future.successful(Conflict(Json.obj("message" -> "User with this email already exists")))
                case None => 
                    userRepository.create(user)
                        .map(createdUser => Ok(Json.toJson(createdUser)))
                        .recover {
                            case ex: Exception => 
                                InternalServerError(Json.obj("message" -> "Error creating user"))
                        }
            }
        )
    }

    def login() = Action.async(parse.json) { request =>
        request.body.validate[LoginRequest].fold(
            errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
            loginRequest => {
                if (loginRequest.email.isEmpty || loginRequest.password.isEmpty) {
                    Future.successful(BadRequest(Json.obj("message" -> "Email and password are required")))
                } else {
                    userRepository.validateUser(loginRequest.email, loginRequest.password)
                        .map {
                            case Some(validUser) => 
                                Ok(Json.obj(
                                    "token" -> JwtUtil.generateToken(validUser.id.get.toString),
                                    "user" -> Json.toJson(validUser.copy(password = ""))
                                ))
                            case None => 
                                Unauthorized(Json.obj("message" -> "Invalid email or password"))
                        }
                        .recover {
                            case ex: Exception =>
                                InternalServerError(Json.obj("message" -> "Error during login"))
                        }
                }
            }
        )
    }
}