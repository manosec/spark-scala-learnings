import utils._

object insertionSort {
  def insertionSort(arr: Array[Int]): Unit = {
    for (i <- 1 until arr.length) {
      val key = arr(i)
      var j = i - 1
      while (j >= 0 && arr(j) > key) {
        arr(j + 1) = arr(j)
        j -= 1
      }
      arr(j + 1) = key
    }
  }

  def main(args: Array[String]): Unit = {
    val arr = Array(64, 34, 25, 12, 22, 11, 90)
    println("Array before sorting: ")
    printElements(arr)
    insertionSort(arr)
    println("\nArray after sorting: ")
    printElements(arr)
  }
}
