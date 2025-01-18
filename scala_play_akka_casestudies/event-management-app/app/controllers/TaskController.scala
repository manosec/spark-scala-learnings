package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import scala.concurrent.{ExecutionContext, Future}
import models.{Task, TaskStatus, TaskAssignmentNotification}
import repositories.TaskRepository
import repositories.EventRepository
import middleware.AuthAction
import notificationService.KafkaProducerService

@Singleton
class TaskController @Inject()(
    taskRepository: TaskRepository,
    kafkaProducer: KafkaProducerService,
    val controllerComponents: ControllerComponents,
    eventRepository: EventRepository,
    authAction: AuthAction
)(implicit ec: ExecutionContext) extends BaseController {


  def getTask(id: Long): Action[AnyContent] = authAction.async {
    taskRepository.getTaskById(id).map {
      case Some(task) => Ok(Json.toJson(task))
      case None => NotFound(Json.obj("message" -> s"Task with id $id not found"))
    }
  }

  def updateTaskStatus(taskId: Long, status: String): Action[AnyContent] = authAction.async {
    try {
      val taskStatus = TaskStatus.withName(status)
      taskRepository.updateStatus(taskId, taskStatus).map {
        case Some(task) => Ok(Json.toJson(task))
        case None => NotFound(Json.obj("message" -> s"Task with id $taskId not found"))
      }
    } catch {
      case _: NoSuchElementException =>
        Future.successful(BadRequest(Json.obj(
          "message" -> s"Invalid status value: $status",
          "allowedValues" -> TaskStatus.values.toSeq.map(_.toString)
        )))
    }
  }

  def getTasksForEvent(eventId: Long): Action[AnyContent] = authAction.async {
    taskRepository.getTasksForEventId(eventId).map { tasks =>
      Ok(Json.toJson(tasks))
    }
  }

  def assignTasks(): Action[JsValue] = authAction.async(parse.json) { request =>
    request.body.validate[List[Task]].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      tasks => {
        taskRepository.assignTasks(tasks).flatMap { createdTasks =>
          val notifications: List[Future[Unit]] = createdTasks.map { task =>
            val notification = TaskAssignmentNotification(
              taskId = task.id.getOrElse(throw new RuntimeException("Task ID not found")),
              eventId = task.eventId,
              teamId = task.teamId,
              taskDescription = task.taskDescription,
              deadLine = task.deadLine
            ) 
            Future.successful(kafkaProducer.sendTaskAssignmentNotification(notification))
          }
          Future.sequence(notifications).map(_ => Created(Json.toJson(createdTasks)))
        }.recover { case ex: Exception =>
          InternalServerError(Json.obj("message" -> s"Failed to assign tasks: ${ex.getMessage}"))
        }
      }
    )
  }
}