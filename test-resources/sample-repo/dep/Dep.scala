package dep

import dep.deeper.DeeperTest

object Dep {
  val list = List(Test.test() + JavaTest.value + DeeperTest.value)
}
