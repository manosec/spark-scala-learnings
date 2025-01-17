import scala.util.control.Breaks._

object conditionaAndJumpStatements {
  def main(args: Array[String]): Unit = {

    //If
    val x = 10
    if (x < 20) {
      println("x is less than 20")
    }

    //If-else
    if (x < 20) {
      println("x is less than 20")
    } else {
      println("x is greater than 20")
    }

    //If-else-if
    if (x < 20) {
      println("x is less than 20")
    } else if (x == 20) {
      println("x is equal to 20")
    } else {
      println("x is greater than 20")
    }

    //Nested If
    val y = 30
    if (x == 10) {
      if (y == 30) {
        println("x is 10 and y is 30")
      }
    }

    //Match
    val i = 2
    i match {
      case 1 => println("One")
      case 2 => println("Two")
      case 3 => println("Three")
      case _ => println("Invalid number")
    }

    //Using && and ||
    val a = 10
    val b = 20
    if (a > 5 && b > 10) {
      println("a is greater than 5 and b is greater than 10")
    } else {
      println("Either a is less than 5 or b is less than 10")
    }

    //try-catch
    try {
      val result = 10 / 0
    } catch {
      case e: ArithmeticException => println("Arithmetic exception")
    } finally {
      println("Finally block")
    }

    /*
    * Scala does not promote explicit use of jump statements like break and continue, promoting the use of functional
    * style, However, we can use break and continue using Breaks and Breaks.breakable
    * */

    println("Simple break example")

    breakable{
      for(i <- 1 to 10){
        if(i == 5){ //Exit loop when i is 5
          break
        }
        println(i)
      }
    }


    for(i <- 10 to 20){
      breakable{
        if(i == 15){ //Skip the iteration when i is 15
          break
        }
        println(i)
      }
    }

  }
}
