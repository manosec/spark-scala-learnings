package model

import play.api.libs.json._
import slick.lifted.{Tag, TableQuery}
import slick.jdbc.MySQLProfile.api._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import scala.util.Try

// Case class for visitor creation requests
case class VisitorRegistration(
    name: String,
    email: String,
    phone: String,
    checkedIn: String,
    checkedOut: String
)

object VisitorRegistration {
    implicit val format: OFormat[VisitorRegistration] = Json.format[VisitorRegistration]
}

case class Visitor(
    id: Int,
    name: String,
    email: String,
    phone: String,
    checkedIn: String,
    checkedOut: String
)

object Visitor {
    // Make the formatter accessible
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    // Make these methods public
    def parseDateTime(dateStr: String): Either[String, LocalDateTime] = {
        Try {
            LocalDateTime.parse(dateStr, dateFormatter)
        }.recoverWith { case _ =>
            Try(LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
        }.toEither.left.map(e => s"Could not parse date: $dateStr. Expected format: yyyy-MM-dd HH:mm:ss or yyyy-MM-dd'T'HH:mm:ss")
    }
    
    def formatDateTime(dateTime: LocalDateTime): String = {
        dateTime.format(dateFormatter)
    }
    
    implicit val localDateTimeReads: Reads[LocalDateTime] = new Reads[LocalDateTime] {
        def reads(json: JsValue): JsResult[LocalDateTime] = json match {
            case JsString(s) => 
                parseDateTime(s) match {
                    case Right(date) => JsSuccess(date)
                    case Left(error) => JsError(error)
                }
            case _ => JsError("String value expected")
        }
    }
    
    implicit val localDateTimeWrites: Writes[LocalDateTime] = new Writes[LocalDateTime] {
        def writes(dateTime: LocalDateTime): JsValue = 
            JsString(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
    }
    
    implicit val format: OFormat[Visitor] = Json.format[Visitor]
    
    def fromRegistration(registration: VisitorRegistration): Either[String, Visitor] = {
        try {
            // Validate the dates are in correct format
            val checkedIn = parseDateTime(registration.checkedIn)
            val checkedOut = parseDateTime(registration.checkedOut)
            
            for {
                inDate <- checkedIn
                outDate <- checkedOut
                _ <- validateDates(inDate, outDate)
            } yield Visitor(
                0,
                registration.name,
                registration.email,
                registration.phone,
                registration.checkedIn,    // Use the original string instead of parsed date
                registration.checkedOut    // Use the original string instead of parsed date
            )
        } catch {
            case e: Exception => Left(e.getMessage)
        }
    }
    
    private def validateDates(checkedIn: LocalDateTime, checkedOut: LocalDateTime): Either[String, Unit] = {
        if (checkedOut.isBefore(checkedIn)) {
            Left("Check-out time cannot be before check-in time")
        } else {
            Right(())
        }
    }
}

class VisitorTable(tag: Tag) extends Table[Visitor](tag, "visitor") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def email = column[String]("email")
    def phone = column[String]("phone")
    def checkedInDate = column[String]("checkedIn")
    def checkedOutDate = column[String]("checkedOut")

    def * = (id, name, email, phone, checkedInDate, checkedOutDate) <> ((Visitor.apply _).tupled, Visitor.unapply)
}