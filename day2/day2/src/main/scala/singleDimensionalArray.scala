object singleDimensionalArray {

  def main(args:Array[String]): Unit = {
    //Array Declaration
    /*
    * Array in scala is a fixed-size data structure that stores elements of the same data type.
    * It is Mutable in nature.
    * */

    val arr1: Array[Int] = new Array[Int](5) //Empty Array of size 5
    val arr2: Array[Int] = Array(1, 2, 3, 4, 5) //Array with elements

    //Array Initialization
    val numbers1 = Array(1, 2, 3, 4, 5)              // Initialize with values
    val numbers2 = new Array[Int](5)

    //Array with repeated values fill method
    val strings = Array.fill(3)("hello")             // Array with repeated value

    // Initial Dimensional Arrays
    val zeros = Array.ofDim[Int](5)

    // Array of zeros
    val range = Array.range(1, 6)


    // Type-specific arrays
    val intArray: Array[Int] = Array(1, 2, 3)
    val stringArray: Array[String] = Array("a", "b", "c")
    val doubleArray: Array[Double] = Array(1.0, 2.0, 3.0)

    //Aceessing elements
    val arr = Array(1, 2, 3, 4, 5)
    println(arr(0)) //1

    //Updating elements
    arr(0) = 10
    println(arr(0)) //10

    //Common Array Operations
    val stringArray1 = Array("a", "b", "c")

    println(stringArray1.length) //3

    println(stringArray1.isEmpty) //false

    //Iterating through an array
    println("Iterating through an array using for loop")
    for (i <- stringArray1) {
      println(i)
    }

    println("Iterating through an array using foreach")
    stringArray1.foreach(println)

    //Array Methods

    val arr3 = Array(1, 2, 3, 4, 5)

    //Slicing
    val slice = arr3.slice(1, 3) //Elements from index 1 to 3
    //Array(2, 3)
    val take = arr3.take(3) //First 3 elements
    //Array(1, 2, 3)

    val drop = arr3.drop(2) //Drop first 2 elements
    //Array(3, 4, 5)

    //Searching
    val index = arr3.indexOf(3) //Index of element 3
    //2

    val contains = arr3.contains(2) //Check if element 3 is present
    //true

    //Sorting
    val sorted = arr3.sorted //Sort the array
    //Array(1, 2, 3, 4, 5)

    val descending = arr3.sorted.reverse
    //Array(5, 4, 3, 2, 1)

    // Transformations
    val transformed = arr3.map(x => x * x) //Square of each element
    //Array(1, 4, 9, 16, 25)

    val filtered = arr3.filter(x => x % 2 == 0) //Filter even numbers
    //Array(2, 4)

    // Concatenation
    val concat = arr3 ++ Array(6, 7, 8) //Concatenate two arrays
    //Array(1, 2, 3, 4, 5, 6, 7, 8)
  }
}
