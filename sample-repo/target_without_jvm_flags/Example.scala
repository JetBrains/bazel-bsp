package target_without_jvm_flags

import java.util.ArrayList

object Example {
  def main(args: Array[String]): Unit = {
    val s = "Sup" + Dep.list.head
    println(s)
  }
}
