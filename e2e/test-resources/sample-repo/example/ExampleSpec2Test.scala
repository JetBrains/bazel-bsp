package example

import dep.Dep
import org.specs2.mutable._

class ExampleSpec2Test extends SpecificationWithJUnit {
  "Test" should {
    "be test" in {
      Dep.list.head
      "Test" must have size (4)
    }
  }
}
