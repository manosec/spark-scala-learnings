object utils {
  def swapElements(arr: Array[Int], i: Int, j: Int): Unit = {
    val temp = arr(i)
    arr(i) = arr(j)
    arr(j) = temp
  }

  def printElements(arr: Array[Int]): Unit = {
    for (i <- arr.indices) {
      print(arr(i) + " ")
    }
  }
}