package models

import play.api.libs.json._
import slick.lifted.{Tag, TableQuery}
import slick.jdbc.MySQLProfile.api._

case class Task(
    id: Option[Long] = None,
    eventId: Long,
    teamId: Long,
    taskDescription: String,
    deadLine: String,
    specialInstructions: Option[String],
    status: TaskStatus.Value,
    createdAt: String
)

object TaskStatus extends Enumeration {
    type TaskStatus = Value
    val PENDING, IN_PROGRESS, COMPLETED, CANCELLED = Value

    implicit val taskStatusColumnType: BaseColumnType[TaskStatus.Value] = 
        MappedColumnType.base[TaskStatus.Value, String](
            e => e.toString,
            s => TaskStatus.withName(s)
        )

    implicit val format: Format[TaskStatus.Value] = Format(
        Reads.enumNameReads(TaskStatus),
        Writes.enumNameWrites
    )
}

object Task {
    implicit val taskFormat: Format[Task] = Json.format[Task]
}

case class TaskTable(tag: Tag) extends Table[Task](tag, "tasks") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def eventId = column[Long]("event_id")
    def teamId = column[Long]("team_id")
    def taskDescription = column[String]("task_description")
    def deadLine = column[String]("dead_line")
    def specialInstructions = column[Option[String]]("special_instructions")
    def status = column[TaskStatus.Value]("status")
    def createdAt = column[String]("created_at")

    def * = (id.?, eventId, teamId, taskDescription, deadLine, specialInstructions, status, createdAt) <> (Task.tupled, Task.unapply)
}

