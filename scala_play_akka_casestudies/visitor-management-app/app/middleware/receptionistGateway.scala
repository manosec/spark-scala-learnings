package middleware

import javax.inject.Inject
import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json.Json
import scala.concurrent.{ExecutionContext, Future}
import security.JwtUtil
import model.PremiseDeptRole

case class UserRequest[A](userId: String, request: Request[A]) extends WrappedRequest[A](request)

class ReceptionistGateway @Inject()(bodyParser: BodyParsers.Default)(implicit ec: ExecutionContext)
    extends ActionBuilder[UserRequest, AnyContent] {

    override def parser: BodyParser[AnyContent] = bodyParser
    override protected def executionContext: ExecutionContext = ec

    override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] = {
        request.headers.get("Authorization") match {
            case Some(authHeader) if authHeader.startsWith("Bearer ") =>
                val token = authHeader.substring(7)
                try {
                    val (userId, role) = JwtUtil.validateToken(token)
                    if (role == PremiseDeptRole.RECEPTIONIST.toString) {
                        block(UserRequest(userId, request))
                    } else {
                        Future.successful(Forbidden(Json.obj(
                            "message" -> "Access restricted to receptionists only",
                            "providedRole" -> role
                        )))
                    }
                } catch {
                    case e: Exception => 
                        Future.successful(Unauthorized(Json.obj(
                            "message" -> "Invalid token",
                            "error" -> e.getMessage
                        )))
                }
            case _ =>
                Future.successful(Unauthorized(Json.obj("message" -> "Authorization header required")))
        }
    }
}