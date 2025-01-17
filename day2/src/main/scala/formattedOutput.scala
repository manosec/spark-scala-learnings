

object formattedOutput {
  def main(args: Array[String]): Unit = {
    //Used to print in the string in new line
    println("Hello there")
    println("Greetings!")

    //Used to print
    print("Hello there")
    print("Greetings!")

    //Used to print formatted output
    /*
    * #### printf (formatted output) with string interpolators
    1. %s - String
    2. %c - Character
    3. %d - Interger
    4. %f - Floating point numbers
    5. %b - Boolean
    * */

    val name = "Mano"
    val char = 'c'
    val random_integer = 123
    val floating_number = 123.456
    val boolean_val = true

    printf("Hi %s", name)
    printf("Character %c", char)
    printf("some integer %d", random_integer)
    printf("some floating number %f", floating_number)
    printf("some boolean value %b", boolean_val)

    //String Interpolators
    println(s"Hi $name")

    //Float Interpolators
    println(f"Hi $floating_number%.2f")

    //Raw Interpolators
    println(raw"Line1\nLine2")

    //.format
    val formatted_string = "My name %s from %s".format(name, "India")

    //String Concatenation
    val country = "India"
    println("My name is " + name + " from + country")

    //Multi-line strings - stripMargin
    val multi_line_string = """This is a multi-line string
                              |This is the second line
                              |This is the third line""".stripMargin

    println(multi_line_string)
  }

}
