package example

import dep.Dep

object Example {
  def main(args: Array[String]): Unit = {
    val s = "Sup" + Dep.list.head
    println(s)
  }
}
