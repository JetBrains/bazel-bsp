package example

import dep.Dep
import java.util.ArrayList

object Example {
  def main(args: Array[String]): Unit = {
    val s = "Sup" + Dep.list.head
    println(s)
  }
}
