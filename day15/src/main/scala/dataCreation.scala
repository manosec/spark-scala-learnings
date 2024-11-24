import java.io.PrintWriter


object dataCreation {
  def main(args: Array[String]): Unit = {
    val fileName = "src/main/data/spark_data.txt"
    val writer = new PrintWriter(fileName)
    val dataLimit = 1000000

    try{
      for(i <- 1 to dataLimit) {
        writer.println(i)
      }

    } finally {
      writer.close()
    }
  }

}
