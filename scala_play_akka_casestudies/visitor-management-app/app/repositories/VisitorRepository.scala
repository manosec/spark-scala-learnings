package repositories

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import model.{Visitor, VisitorTable}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import play.api.Logger
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

class VisitorRepository @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) 
    extends Repository[Visitor] {
    
    import dbConfig.profile.api._
    private val visitorTable = TableQuery[VisitorTable]
    private val logger = Logger(this.getClass)

    implicit val localDateTimeColumnType: BaseColumnType[LocalDateTime] = MappedColumnType.base[LocalDateTime, String](
        dateTime => dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        str => LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    )

    def findAll(): Future[Seq[Visitor]] =
        db.run(visitorTable.result)
        .map { visitors =>
            println(visitors)
            visitors.foreach(visitor => logger.info(s"Fetched Visitor: $visitor"))
            visitors
        }
        .recover {
            case ex: Exception =>
                logger.error("Error fetching visitors", ex)
                throw ex
        }

    def findById(id: Int): Future[Option[Visitor]] = {
    db.run(visitorTable.filter(_.id === id).result.headOption).map {
        case Some(visitor) =>
            // Log the raw data
            println(s"Fetched Visitor: ${visitor.id}, ${visitor.name}, ${visitor.email}, ${visitor.phone}, ${visitor.checkedIn}, ${visitor.checkedOut}")
            Some(visitor)
        case None =>
            println(s"No visitor found with ID: $id")
            None
    }.recover {
        case ex: Exception =>
            println(s"Error fetching visitor by ID: $id - ${ex.getMessage}")
            None
        }
    }
    
    def create(visitor: Visitor): Future[Visitor] = 
        db.run((visitorTable returning visitorTable.map(_.id)) += visitor)
          .map(id => visitor.copy(id = id))

    def update(visitor: Visitor): Future[Int] = 
        db.run(visitorTable.filter(_.id === visitor.id).update(visitor))

    def delete(id: Int): Future[Int] = 
        db.run(visitorTable.filter(_.id === id).delete)
} 