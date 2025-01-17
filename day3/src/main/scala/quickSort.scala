import utils._

object quickSort {

  def partition(arr: Array[Int], low: Int, high: Int): Int = {
    val pivot = arr(high)
    var i = low - 1
    for (j <- low until high) {
      if (arr(j) < pivot) {
        i += 1
        swapElements(arr, i, j)
      }
    }
    swapElements(arr, i + 1, high)
    i + 1
  }

  def quickSort(arr: Array[Int], low: Int, high: Int): Unit = {
    if (low < high) {
      val pi = partition(arr, low, high)
      quickSort(arr, low, pi - 1)
      quickSort(arr, pi + 1, high)
    }
  }

  def main(args: Array[String]): Unit = {
    val arr = Array(64, 25, 12, 22, 11)
    println("Array before sorting: ")
    printElements(arr)
    quickSort(arr, 0, arr.length - 1)
    println("\nArray after sorting: ")
    printElements(arr)
  }
}
