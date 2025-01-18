import utils.EmailUtils


object EmailTest {
    def main(args: Array[String]): Unit = {
        println("Sending email...")
        EmailUtils.sendEmail("manoranjan.n@payoda.com", "Test Email", "This is a test email")
        println("Email sent successfully")
    }
}