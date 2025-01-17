object multiDimensionalArray {
  def main(args:Array[String]): Unit = {
    val arr2d = Array.ofDim[Int](3, 4)
    val matrix2 = Array.fill(3,4)(-1)
    val matrix3 = Array(
      Array(1, 2, 3, 4),
      Array(5, 6, 7, 8),
      Array(9, 10, 11, 12)
    )
    val arr3d = Array.ofDim[Int](2, 3, 4)
    val multiDimArr = Array(
      Array(1, 2),
      Array(3, 4, 5),
      Array(6, 7, 8, 9)
    )

    //Accessing elements
    val arr = Array(
      Array(1, 2, 3),
      Array(4, 5, 6),
      Array(7, 8, 9)
    )

    println(arr(0)(0)) //1

    //Updating elements
    arr(0)(0) = 10
    println(arr(0)(0)) //10

    //Common Array Operations
    val matrix = Array(
      Array(1, 2, 3),
      Array(4, 5, 6),
      Array(7, 8, 9)
    )

    println(matrix.length) //3

    //Iterating through an using for loop
    for {
      i <- matrix.indices
      j <- matrix(i).indices
    } println(s"matrix($i)($j) = ${matrix(i)(j)}")

    //Iterating through an array using foreach
    matrix.foreach(row => row.foreach(println))


    //Array Methods
    val matrix1 = Array(
      Array(1, 2, 3),
      Array(4, 5, 6),
      Array(7, 8, 9)
    )

    val transposedMatricmatrix = matrix1.transpose
    println(transposedMatricmatrix.deep.mkString("\n"))


    val elementTransformation = matrix1.map(row => row.map(x => x * x * x))
    println(elementTransformation.deep.mkString("\n"))

  }
}
