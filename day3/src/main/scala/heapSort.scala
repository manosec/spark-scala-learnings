import utils._

object heapSort {

  def heapify(ints: Array[Int], i: Int, i1: Int): Unit = {
    var largest = i1
    val l = 2 * i1 + 1
    val r = 2 * i1 + 2
    if (l < i && ints(l) > ints(largest)) {
      largest = l
    }
    if (r < i && ints(r) > ints(largest)) {
      largest = r
    }
    if (largest != i1) {
      swapElements(ints, i1, largest)
      heapify(ints, i, largest)
    }
  }

  def heapSort(arr: Array[Int]): Unit = {
    val n = arr.length
    for (i <- n / 2 - 1 to 0 by -1) {
      heapify(arr, n, i)
    }
    for (i <- n - 1 to 1 by -1) {
      swapElements(arr, 0, i)
      heapify(arr, i, 0)
    }
  }

  def main(args: Array[String]): Unit = {
    val arr = Array(64, 34, 25, 12, 22, 11, 90)
    println("Array before sorting: ")
    printElements(arr)
    heapSort(arr)
    println("\nArray after sorting: ")
    printElements(arr)
  }

}
