package actors

import akka.actor.Actor
import models.VisitorManagementConsumerModel
import java.io.{FileWriter, PrintWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import utils.EmailUtils

class VisitorManagementActor extends Actor {
    
    def receive: Receive = {
        case "init" => sender() ! "ack"  // Handle initialization message
        case "ack" => // Handle acknowledgment
        case Right(message: VisitorManagementConsumerModel) =>
            println(s"Received visitor management message: $message")
            sendNotification(message)
            sender() ! "ack"
        case Left(error) =>
            println(s"Received error in VisitorManagementActor: $error")
            sender() ! "ack"
        case other =>
            println(s"Received unexpected message in VisitorManagementActor: $other")
            sender() ! "ack"
    }

    def sendNotification(message: VisitorManagementConsumerModel): Unit = {
        val notification = createNotification(message)
        pushNotification(notification)
    }

    private def createNotification(message: VisitorManagementConsumerModel): String = {
        val notification = s"Visitor Notification: Visitor '${message.visitorName}' with email '${message.visitorEmail}' has checkedIn."
        println(notification)
        notification
    }

    private def pushNotification(message: String): Unit = {
        // Only send email
        EmailUtils.sendEmail("manoranjan.n@payoda.com", "Visitor Notification", message)
    }
}