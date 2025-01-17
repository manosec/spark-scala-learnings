trait Task {
  def doTask(): Unit = {
    println("Task doTask")
  }
}

trait Cook extends Task {
  abstract override def doTask(): Unit = {
    super.doTask()
    println("Cook doTask")
  }
}

trait Garnish extends Cook {
  abstract override def doTask(): Unit = {
    super.doTask()
    println("Garnish doTask")
  }
}

trait Pack extends Garnish {
  abstract override def doTask(): Unit = {
    super.doTask()
    println("Pack doTask")
  }
}

class Activity extends Task {
  def doActivity(): Unit = {
    doTask() // Calls the overridden `doTask` in traits
  }
}

@main def main(): Unit = {
  val task: Task = new Activity with Cook with Garnish with Pack
  task.asInstanceOf[Activity].doActivity()
}
