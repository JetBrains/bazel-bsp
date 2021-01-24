import dep.Dep

object ExampleTest {
  def test(): Unit = {
    val s = "Sup" + Dep.list.head
    println(s)
  }
}
