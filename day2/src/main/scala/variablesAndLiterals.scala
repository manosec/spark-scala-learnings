object variablesAndLiterals {
  def main(args: Array[String]): Unit = {
    // Mutable variables
    var mutable_variable: Int = 10
    mutable_variable = 20

    //Immutable variables
    val immutable_variable: Int = 10
    //immutable_variable = 20 // This will throw an error

    //Integer Literals
    val int_literal = 10
    val long_literal = 10L

    println(int_literal)
    println(long_literal)

    //Floating point literals
    val float_literal = 10.01123142352F // F or f is used to specify float
    val double_literal = 10.123412313253252532d // D or d is used to specify double

    println(float_literal)

    println(double_literal)

    //Boolean Literals
    val boolean_literal = true
    val boolean_literal2 = false

    println(boolean_literal)
    println(boolean_literal2)

    //Character Literals
    val char_literal = 'a'  // Single quotes
    val char_literal2 = '\u0041' // Unicode for A

    println(char_literal)
    println(char_literal2)

    //String Literals
    val string_literal = "Hello, World!"
    val string_literal2 =
      """Hello,
        World!""" // Multi-line string"

    println(string_literal)
    println(string_literal2)

    //Null Literals
    val null_literal = null

    println(null_literal)

    //Unit Literals
    val unit_literal = () // Equivalent to void in C++, java

    println(unit_literal)

  }
}
