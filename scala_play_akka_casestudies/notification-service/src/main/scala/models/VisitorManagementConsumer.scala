package models

import spray.json._

case class VisitorManagementConsumerModel(
  visitorId: Int,
  visitorName: String,
  visitorEmail: String,
  notificationType: String,
  timestamp: Long = System.currentTimeMillis()
)

object VisitorManagementConsumerModel extends DefaultJsonProtocol {
  implicit val visitorManagementConsumerModelFormat: RootJsonFormat[VisitorManagementConsumerModel] = 
    jsonFormat5(VisitorManagementConsumerModel.apply)
}   