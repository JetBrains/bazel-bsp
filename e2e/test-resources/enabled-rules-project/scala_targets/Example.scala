package example

object Example extends App{
  val aa: A = ???
  aa match {
    case B(_) =>
  }
}

sealed trait A
case class B(b: Int) extends A
case class C(c: Int) extends A
