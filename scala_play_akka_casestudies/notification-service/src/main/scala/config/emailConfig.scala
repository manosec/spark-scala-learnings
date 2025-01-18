package config

object EmailConfig {
  val senderName = sys.env.getOrElse("SENDER_NAME", "")
  val senderEmail = sys.env.getOrElse("SENDER_EMAIL", "")
  val senderPassword = sys.env.getOrElse("SENDER_PASSWORD", "password")
  val smtpHost = "smtp.gmail.com"
  val smtpPort = "587"
  
}
