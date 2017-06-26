package levsha

import levsha.impl.DiffRenderContext
import levsha.impl.DiffRenderContext.DummyChangesPerformer

import scala.collection.mutable
import scala.language.implicitConversions

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
        original = "m",
        updated = 'input('disabled /= "", 'div('name /= "cow"))
      )
      assert(
        changes == Seq(
          create("1", "input"),
          setAttr("1", "disabled", ""),
          create("1_1", "div"),
          setAttr("1_1", "name", "cow")
        )
      )
    }

    "should text to node with chidlren" - {
      val changes = runDiff(
        original = {
          'div('class /= "world",
            'input('class /= "world",'lang /= "ru",
              "j"
            ),
            'p('class /= "world",'lang /= "ru"),
            "I",
            "j",
            "b",
            "V",
            "A",
            "d",
            "d",
            "o",
            'p('class /= "world")
          )
        },
        updated = {
          'div('class /= "world",
            'input('class /= "world",'lang /= "ru",
              "j"
            ),
            'p('class /= "world",'lang /= "ru"),
            "I",
            'div('lang /= "ru"),
            "b",
            'span(),
            "A",
            "d",
            "d",
            "o",
            'p('class /= "world")
          )
        }
      )
      assert {
        changes == Seq(
          create("1_4","div"),
          setAttr("1_4","lang","ru"),
          create("1_6","span")
        )
      }
    }

    "should remove attribute" - {
      val changes = runDiff(
        original = { 'span('class /= "world",'style /= "margin: 10;", "q") },
        updated = { 'span('style /= "margin: 10;", "q") }
      )
      assert(changes == Seq(removeAttr("1", "class")))
    }
    
    "should remove only subroot, not entire tree" - {
      val changes = runDiff(
        original = {
          'div('class /= "world",
            'div('class /= "world"),
            'div('class /= "hello",
              'span('name /= "cow",'class /= "hello",
                'span()
              ),
              'div('class /= "world",'style /= "margin: 10;")
            ),
            'button('class /= "world",
              'span('style /= "margin: 10;",'class /= "hello"),
              'span(),
              'div('lang /= "ru",'name /= "cow")
            ),
            "dasd"
          )
        },
        updated = {
          'div('class /= "world",
            'div('class /= "world"),
            "gGi"
          )
        }
      )
      assert {
        changes == Seq(
          createText(List(1, 2), "gGi"),
          remove(List(1, 3)),
          remove(List(1, 4))
        )
      }
    }
  }

  // -----------------------

  def runDiff(original: Document[Nothing], updated: Document[Nothing]): Seq[Change] = {
    val performer = new DiffTestChangesPerformer()
    val renderContext = DiffRenderContext[Nothing]()
    original(renderContext)
    renderContext.diff(DummyChangesPerformer)
    renderContext.swap()
    updated(renderContext)
    renderContext.diff(performer)
    performer.result
  }
}
