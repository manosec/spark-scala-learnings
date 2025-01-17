import utils._

object bubbleSort {

  def bubbleSort(arr: Array[Int]): Unit = {
    for (i <- 0 until arr.length - 1) {
      for (j <- 0 until arr.length - i - 1) {
        if (arr(j) > arr(j + 1)) {
          swapElements(arr, j, j + 1)
        }
      }
    }
  }

  def main(args: Array[String]): Unit = {
    val arr = Array(64, 34, 25, 12, 22, 11, 90)
    println("Array before sorting: ")
    printElements(arr)
    bubbleSort(arr)
    println("\nArray after sorting: ")
    printElements(arr)
  }

}
