package models

import spray.json._

case class EventManagementConsumerModel(
  taskId: Int,
  eventId: Int,
  teamId: Int,
  taskDescription: String,
  deadLine: String
) 

object EventManagementConsumerModel extends DefaultJsonProtocol {
  implicit val eventManagementConsumerModelFormat: RootJsonFormat[EventManagementConsumerModel] = 
    jsonFormat5(EventManagementConsumerModel.apply)
}