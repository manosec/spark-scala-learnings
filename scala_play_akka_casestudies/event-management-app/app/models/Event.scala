package models

import play.api.libs.json._
import slick.jdbc.MySQLProfile.api._
import java.time.LocalDate
import play.api.libs.json.JsString
import java.time.format.DateTimeFormatter

case class Event(
    id: Option[Long] = None,
    title: String,
    description: String,
    date: LocalDate,
    location: String,
    organizerId: String,
    capacity: Int,
    status: EventStatus.Value
)

object Event {
    // Define the date formats we want to support
    private val dateFormatters = List(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    )

    implicit val localDateReads: Reads[LocalDate] = new Reads[LocalDate] {
        def reads(json: JsValue): JsResult[LocalDate] = json match {
            case JsString(s) => {
                val parseResult = dateFormatters.foldLeft[Option[LocalDate]](None) { (result, formatter) =>
                    result.orElse {
                        try {
                            Some(LocalDate.parse(s, formatter))
                        } catch {
                            case _: Exception => None
                        }
                    }
                }
                
                parseResult match {
                    case Some(date) => JsSuccess(date)
                    case None => JsError(s"error.invalid.date: Unable to parse '$s'")
                }
            }
            case _ => JsError("error.expected.date")
        }
    }

    implicit val localDateWrites: Writes[LocalDate] = new Writes[LocalDate] {
        def writes(d: LocalDate): JsValue = JsString(d.format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    implicit val eventFormat: Format[Event] = Json.format[Event]
}

object EventStatus extends Enumeration {
    type EventStatus = Value
    val Scheduled, Cancelled, Completed = Value

    implicit val eventStatusColumnType: BaseColumnType[EventStatus.Value] = 
        MappedColumnType.base[EventStatus.Value, String](
            e => e.toString,
            s => EventStatus.withName(s)
        )

    implicit val format: Format[EventStatus] = Format(
        Reads.enumNameReads(EventStatus),
        Writes.enumNameWrites
    )
}

case class EventTable(tag: Tag) extends Table[Event](tag, "events") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def description = column[String]("description")
    def date = column[LocalDate]("date")
    def location = column[String]("location")
    def organizerId = column[String]("organizer_id")
    def capacity = column[Int]("capacity")
    def status = column[EventStatus.Value]("status")

    def * = (id.?, title, description, date, location, organizerId, capacity, status) <> (Event.tupled, Event.unapply)
}