package utils

import java.util.Properties
import javax.mail._
import javax.mail.internet._
import javax.mail.internet.InternetAddress
import config.EmailConfig

object EmailUtils {

  // Email configuration
  val smtpHost = EmailConfig.smtpHost
  val smtpPort = EmailConfig.smtpPort
  val senderEmail = EmailConfig.senderEmail
  val senderName = EmailConfig.senderName
  val senderPassword = EmailConfig.senderPassword

  def sendEmail(toEmail: String, subject: String, body: String): Unit = {
    val properties = new Properties()
    properties.put("mail.smtp.host", smtpHost) 
    properties.put("mail.smtp.port", smtpPort)
    properties.put("mail.smtp.auth", "true")
    properties.put("mail.smtp.starttls.enable", "true")
    properties.put("mail.smtp.socketFactory.port", "465")
    properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")

    // Create a Session with the email properties and authentication
    val session = Session.getInstance(properties, new Authenticator {
      override def getPasswordAuthentication: PasswordAuthentication = {
        new PasswordAuthentication(senderEmail, senderPassword)
      }
    })

    try {
      // Create a new MimeMessage object
      val message = new MimeMessage(session)

      // Set the recipient, sender, subject, and content
      message.setFrom(new InternetAddress(senderEmail, senderName)) // Include name in "From" field
      message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail))
      message.setSubject(subject)
      message.setText(body)

      // Send the email
      Transport.send(message)
    } catch {
      case e: MessagingException =>
        println(s"Failed to send email: ${e.getMessage}")
    }
  }
}