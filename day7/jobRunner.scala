case class JobRunner(str: String, timeInSeconds: Int)

object JobRunner {

  def apply(job: String, timeInSeconds: Int)(logicalBlock: (String, Int) => String): Unit = {
    val baseTime = System.currentTimeMillis() / 1000

    while (System.currentTimeMillis() / 1000 - baseTime < timeInSeconds) {
      println("Processing......")
      Thread.sleep(1000)
    }

    println(logicalBlock(job, timeInSeconds))
  }
}

@main def processTask(): Unit = {
  JobRunner("filtering", 10) { (name, time) =>
    println(s"Took $time sec to complete $name job ")
    s"Job $name completed"
  }
}
