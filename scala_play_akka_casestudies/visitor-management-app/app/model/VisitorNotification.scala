package model

import play.api.libs.json._

case class VisitorNotification(
  visitorId: Int,
  visitorName: String,
  visitorEmail: String,
  notificationType: String,
  timestamp: Long = System.currentTimeMillis()
)

object VisitorNotification {
  implicit val format: Format[VisitorNotification] = Json.format[VisitorNotification]
} 