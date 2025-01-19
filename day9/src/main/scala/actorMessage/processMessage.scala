package actorMessage

import spray.json._
import DefaultJsonProtocol._

case class processMessage(message: String, messageKey: String)

object JsonFormats {
  implicit val processMessageFormat: RootJsonFormat[processMessage] = jsonFormat2(processMessage)
}
