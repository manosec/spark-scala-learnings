object dataTypes {
  def main(args: Array[String]): Unit = {
    //Integer
    val int_variable: Int = 10
    val int_variable2 = 20

    println(int_variable)
    println(int_variable2)

    //Long
    val long_variable: Long = 100000000000L
    val long_variable2 = 200000000000L

    println(long_variable)
    println(long_variable2)

    //Float
    val float_variable: Float = 10.0f
    val float_variable2 = 20.0f

    println(float_variable)
    println(float_variable2)

    //Double
    val double_variable: Double = 10.0
    val double_variable2 = 20.0

    println(double_variable)
    println(double_variable2)

    //Boolean
    val boolean_variable: Boolean = true
    val boolean_variable2 = false

    println(boolean_variable)
    println(boolean_variable2)

    //Character
    val char_variable: Char = 'a'
    val char_variable2 = 'b'

    println(char_variable)
    println(char_variable2)

    //String
    val string_variable: String = "Hello, World!"
    val string_variable2 = "Hello, World!"

    println(string_variable)
    println(string_variable2)

    //Null
    val null_variable: Null = null

    println(null_variable)

    //Unit
    val unit_variable: Unit = ()

    println(unit_variable)

    //Any
    val any_variable: Any = 10
    val any_variable2: Any = "Hello, World!"

    println(any_variable)
    println(any_variable2)

    //AnyRef
    val anyRef_variable: AnyRef = "Hello, World!"

    println(anyRef_variable)

    //Nothing
//    val nothing_variable: Nothing = throw new Exception("This is an exception")


  }
}
