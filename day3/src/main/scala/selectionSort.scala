import utils._

object selectionSort {
  def selectionSort(arr: Array[Int]): Unit = {
    for (i <- 0 until arr.length - 1) { // Start from 0
      var minIdx = i
      for (j <- i + 1 until arr.length) {
        if (arr(j) < arr(minIdx)) {
          minIdx = j
        }
      }
      swapElements(arr, i, minIdx)
    }
  }

  def main(args: Array[String]): Unit = {
    val arr = Array(64, 25, 12, 22, 11)
    println("Array before sorting: ")
    printElements(arr)
    selectionSort(arr)
    println("\nArray after sorting: ")
    printElements(arr)
  }

}
