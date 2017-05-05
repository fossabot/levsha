package levsha

import levsha.impl.DiffRenderContext.ChangesPerformer

import scala.collection.mutable

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
sealed trait Change {
  def id: List[Int]
}

object Change {

  implicit def parseId(s: String): List[Int] = s.split('_').toList.map(_.toInt)

  final class DiffTestChangesPerformer extends ChangesPerformer {
    private val buffer = mutable.Buffer.empty[Change]
    def removeAttr(id: String, name: String): Unit =
      buffer += Change.removeAttr(id, name)
    def remove(id: String): Unit =
      buffer += Change.remove(id)
    def setAttr(id: String, name: String, value: String): Unit =
      buffer += Change.setAttr(id, name, value)
    def createText(id: String, text: String): Unit =
      buffer += Change.createText(id, text)
    def create(id: String, tag: String): Unit =
      buffer += Change.create(id, tag)
    def result: Seq[Change] = buffer
  }

  case class removeAttr(id: List[Int], name: String) extends Change
  case class remove(id: List[Int]) extends Change
  case class setAttr(id: List[Int], name: String, value: String) extends Change
  case class createText(id: List[Int], text: String) extends Change
  case class create(id: List[Int], tag: String) extends Change

  implicit val ordering = new Ordering[Change] {
    private val iterableIntOrdering = implicitly[Ordering[Iterable[Int]]]
    def compare(x: Change, y: Change): Int = {
      iterableIntOrdering.compare(x.id, y.id)
    }
  }
}