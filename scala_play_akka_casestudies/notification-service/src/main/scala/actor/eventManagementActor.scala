package actors

import akka.actor.Actor
import models.EventManagementConsumerModel
import java.io.{FileWriter, PrintWriter}
import utils.EmailUtils

class EventManagementActor extends Actor {
  def receive: Receive = {
    case "init" => sender() ! "ack"  // Handle initialization message
    case "ack" => // Handle acknowledgment
    case Right(message: EventManagementConsumerModel) =>
      println(s"Received event management message: $message")
      sendNotification(message)
      sender() ! "ack"
    case Left(error) =>
      println(s"Received error in EventManagementActor: $error")
      sender() ! "ack"
    case other =>
      println(s"Received unexpected message in EventManagementActor: $other")
      sender() ! "ack"
  }
  
  def sendNotification(message: EventManagementConsumerModel): Unit = {
    val notification = createNotification(message)
    pushNotification(notification)
  }

  def createNotification(message: EventManagementConsumerModel): String = {
    val notification = s"Task Notification: Task ID ${message.taskId} for Event ID ${message.eventId}" +
    s"\nTeam ID: ${message.teamId}" +
    s"\nDescription: ${message.taskDescription}" +
    s"\nDeadline: ${message.deadLine}"

    notification
  }

  private def pushNotification(message: String): Unit = {
    // Only send email
    EmailUtils.sendEmail("manoranjan.n@payoda.com", "Event Notification", message)
  }
}