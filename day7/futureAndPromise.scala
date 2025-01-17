import scala.concurrent.{Future, Promise, Await}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure, Random}
import java.util.concurrent.atomic.AtomicBoolean


object Config {
  val GOAL_NUMBER: Int = 10
  val THREAD_COUNT: Int = 50
  val MAX_RANDOM: Int = 10
}

def generateRandom: Int = {
  1 + Random.nextInt(Config.MAX_RANDOM)
}

def executeRandomNumberTask(): Future[String] = {
  val taskCompletion = Promise[String]()

  // Ensures thread-safe tracking of whether the task is completed
  val taskCompleted = new AtomicBoolean(false)

  def createWorker(workerId: Int) = new Thread(new Runnable {
    override def run(): Unit = {
      var randomNumber = -1

      while (!taskCompleted.get() && randomNumber != Config.GOAL_NUMBER) {
        randomNumber = generateRandom

        if (randomNumber == Config.GOAL_NUMBER) {
          if (taskCompleted.compareAndSet(false, true)) {
            taskCompletion.success(s"Worker $workerId succeeded by generating the goal number: ${Config.GOAL_NUMBER}")
          }
        }
      }
    }
  })

  // Create and start worker threads
  val workers = (1 to Config.THREAD_COUNT).map(workerId => createWorker(workerId))
  workers.foreach(_.start())

  taskCompletion.future
}

@main def runGoalFinderTask(): Unit = {
  val result: Future[String] = executeRandomNumberTask()

  result.onComplete {
    case Success(message) => println(message)
    case Failure(exception) => println(s"Task failed with exception: $exception")
  }

  Await.result(result, Duration.Inf) // Wait until the future completes
}
