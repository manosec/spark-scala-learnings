package repositories

import models.{Event, EventStatus, EventTable}
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.MySQLProfile
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.MySQLProfile.api._
import java.time.LocalDate
import java.sql.Timestamp
import java.time.LocalDateTime

@Singleton
class EventRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
    (implicit ec: ExecutionContext)
    extends HasDatabaseConfigProvider[MySQLProfile] {

    private val events = TableQuery[EventTable]

    def create(event: Event): Future[Event] = {
        val insertAction = (events returning events.map(_.id)
            into ((event, id) => event.copy(id = Some(id)))
        ) += event

        db.run(insertAction).recover {
            case ex: Exception => 
                throw new RuntimeException(s"Error creating event: ${ex.getMessage}")
        }
    }

  def getEventById(id: Long): Future[Option[Event]] = {
    db.run(events.filter(_.id === id).result.headOption).map { eventOpt =>
        eventOpt.map { event =>
            // Convert any database timestamp to the correct format
            event.copy(
                date = event.date
            )
        }
    }.recover {
        case ex: Exception =>
            throw ex
    }
  }

  def update(id: Long, event: Event): Future[Event] = {
    val updateQuery = events.filter(_.id === id)
        .map(e => (e.title, e.description, e.date, e.location, e.organizerId, e.capacity, e.status))
        .update((event.title, event.description, event.date, event.location, event.organizerId, event.capacity, event.status))
    
    db.run(updateQuery).flatMap { _ =>
        getEventById(id).map(_.getOrElse(throw new RuntimeException(s"Event $id not found")))
    }
  }

  def listEvents(
      status: Option[EventStatus.Value] = None, 
      date: Option[LocalDate] = None
  ): Future[Seq[Event]] = {
    val query = events
      .filterOpt(status) { case (event, s) => event.status == s }
      .filterOpt(date) { case (event, d) => event.date == d }

    db.run(query.result)
  }

  def checkEventExists(date: LocalDate, slot: Int): Future[Boolean] = {
    db.run(events.filter(event => event.date == date).exists.result)
  }

  def getEventsByDate(date: LocalDate): Future[Seq[Event]] = {
    db.run(events.filter(event => event.date == date).result)
  }

  def delete(eventId: Long): Future[Int] = {
    db.run(events.filter(_.id === eventId).delete)
        .recover {
            case ex: Exception => 
                throw new RuntimeException(s"Failed to delete event $eventId: ${ex.getMessage}")
        }
  }

}
