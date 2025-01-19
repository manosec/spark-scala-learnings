package models

import spray.json._
import DefaultJsonProtocol._

case class Message(messageType: String, message: String, messageKey: String)
case class ProcessedMessage(message: String, messageKey: String)

object MessageJsonProtocol extends DefaultJsonProtocol {
  implicit val messageFormat: RootJsonFormat[Message] = jsonFormat3(Message)
  implicit val processedMessageFormat: RootJsonFormat[ProcessedMessage] = jsonFormat2(ProcessedMessage)
}