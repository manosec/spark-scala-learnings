object iterativeStatements {
  def main(args: Array[String]): Unit = {
    //For loop
    for (i <- 1 to 5) {
      println(i)
    }

    //For loop with until
    for (i <- 1 until 5) {
      println(i)
    }

    //For loop with until and by
    for (i <- 1 until 10 by 2) {
      println(i)
    }

    //While loop
    var x = 0
    while (x < 5) {
      println(x)
      x += 1
    }

    //Do-While loop
    var y = 0
    do {
      println(y)
      y += 1
    } while (y < 5)


  }
}
