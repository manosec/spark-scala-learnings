object selectionStatements {
  val x = 10
  val y = 20

  /*
  * Selection Statements
  * A selection statement is a programming construct that allows a program to make decisions and execute specific blocks of code based on certain conditions.
  * It is a key part of control flow in programming and helps determine which path of execution to follow.
  * */

  //If statement
  if (x < 20) {
    println("x is less than 20")
  }

  //If-else statement
  if (x < 20) {
    println("x is less than 20")
  } else {
    println("x is greater than 20")
  }

  //Else-if statement
  if (x < 20) {
    println("x is less than 20")
  } else if (x == 20) {
    println("x is equal to 20")
  } else {
    println("x is greater than 20")
  }

  //Match statement
  val i = 2
  i match {
    case 1 => println("One")
    case 2 => println("Two")
    case 3 => println("Three")
    case _ => println("Invalid number")
  }
}
