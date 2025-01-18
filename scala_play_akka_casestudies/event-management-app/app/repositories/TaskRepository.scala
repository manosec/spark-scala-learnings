package repositories

import models.{Task, TaskTable, TaskStatus}
import scala.concurrent.{ExecutionContext, Future}
import slick.jdbc.MySQLProfile
import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.MySQLProfile.api._
import play.api.libs.json.Json

@Singleton
class TaskRepository @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider,
    implicit val ec: ExecutionContext
) extends HasDatabaseConfigProvider[MySQLProfile] {

  private val tasks = TableQuery[TaskTable]

  def create(task: Task): Future[Long] = {
    val insertQueryThenReturnId = tasks returning tasks.map(_.id)
    db.run(insertQueryThenReturnId += task).recover {
      case ex: Exception => 
        throw new RuntimeException(s"Failed to create task: ${ex.getMessage}")
    }
  }

  def getTaskById(taskId: Long): Future[Option[Task]] = {
    db.run(tasks.filter(_.id === taskId).result.headOption).recover {
      case ex: Exception =>
        throw new RuntimeException(s"Failed to fetch task $taskId: ${ex.getMessage}")
    }
  }

  def updateStatus(taskId: Long, status: TaskStatus.Value): Future[Option[Task]] = {
    val action = for {
      updated <- tasks.filter(_.id === taskId)
        .map(_.status)
        .update(status)
      task <- if (updated > 0) tasks.filter(_.id === taskId).result.headOption
              else DBIO.successful(None)
    } yield task
    
    db.run(action.transactionally).recover {
      case ex: Exception =>
        throw new RuntimeException(s"Failed to update task $taskId: ${ex.getMessage}")
    }
  }

  def getTasksForEventId(eventId: Long): Future[Seq[Task]] = {
    db.run(tasks.filter(_.eventId === eventId).result).recover {
      case ex: Exception =>
        throw new RuntimeException(s"Failed to fetch tasks for event $eventId: ${ex.getMessage}")
    }
  }

  def assignTasks(tasksList: List[Task]): Future[List[Task]] = {
    val actions = tasksList.map { task =>
      (tasks returning tasks.map(_.id) into ((task, id) => task.copy(id = Some(id)))) += task
    }

    db.run(DBIO.sequence(actions).transactionally).recover {
      case ex: Exception =>
        throw new RuntimeException(s"Failed to assign tasks: ${ex.getMessage}")
    }
  }
}
