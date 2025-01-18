package models

import play.api.libs.json._

case class TaskAssignmentNotification(
  taskId: Long,
  eventId: Long,
  teamId: Long,
  taskDescription: String,
  deadLine: String
)

object TaskAssignmentNotification {
  implicit val format: Format[TaskAssignmentNotification] = Json.format[TaskAssignmentNotification]
}