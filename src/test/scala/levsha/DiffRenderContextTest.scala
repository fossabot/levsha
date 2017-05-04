package levsha

import levsha.impl.DiffRenderContext

import scala.collection.mutable

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
object DiffRenderContextTest extends utest.TestSuite {

  val dsl = new TemplateDsl[Nothing]()

  import utest.{TestableString, TestableSymbol => _}
  import Change._
  import dsl._

  val tests = this {

    "should replace text to node" - {
      val changes = runDiff(
        original = { implicit rc => "m" },
        updated = { implicit rc =>
          'input('div('name /= "cow"))
        }
      )
      assert(
        changes == Seq(
          create("1", "input"),
          create("1_1", "div"),
          setAttr("1_1", "name", "cow")
        )
      )
    }

    "should remove attribute" - {
      val changes = runDiff(
        original = { implicit rc => 'span('class /= "world",'style /= "margin: 10;", "q") },
        updated = { implicit rc =>'span('style /= "margin: 10;", "q") }
      )
      assert(changes == Seq(removeAttr("1", "class")))
    }

  }

  // -----------------------

  def runDiff(original: RenderContext[Nothing] => RenderUnit, updated: RenderContext[Nothing] => RenderUnit): Seq[Change] = {
    val identIndex = mutable.Map.empty[Int, String]
    val performer = new DiffTestChangesPerformer()
    val rc1 = new DiffRenderContext[Nothing](identIndex = identIndex)
    val rc2 = new DiffRenderContext[Nothing](identIndex = identIndex)
    original(rc1)
    updated(rc2)
    rc2.diff(rc1, performer)
    performer.result
  }
}
