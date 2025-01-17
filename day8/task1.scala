
// Base trait
trait GetStarted {
  def prepare(): Unit = { // Not abstract, implemented method
    println("Getting started")
  }
}

// Trait that extends GetStarted
trait Cook extends GetStarted {
  abstract override def prepare(): Unit = { // Needs abstract override
    super.prepare()
    println("Cooking")
  }
}

// Trait that extends GetStarted
trait Seasoning {
  def applySeasoning(): Unit = {
    println("Seasoning")
  }
}

// Class that extends Cook and Seasoning
class Food extends Cook with Seasoning {
  def prepareFood(): Unit = {
    prepare() // Calls prepare from Cook and GetStarted
    applySeasoning() // Calls Seasoning
  }
}

@main def main(): Unit = {
  val food = new Food
  food.prepareFood()
}

