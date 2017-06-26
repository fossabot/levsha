package levsha

import scala.language.experimental.macros
import scala.language.implicitConversions

/**
  * @author Aleksey Fomkin <aleksey.fomkin@gmail.com>
  */
class TemplateDsl[MiscType] {

  import Document._

  /** Converts String to text document node */
  implicit def stringToNode(value: String): Node[MiscType] =
    Node { rc => rc.addTextNode(value) }

  /** Converts MiscType to text document node */
  implicit def miscToNode(value: MiscType): Node[MiscType] =
    Node { rc => rc.addMisc(value) }

  /** Converts iterable of templates to document fragment */
  implicit def seqToNode(xs: Iterable[Node[MiscType]]): Node[MiscType] =
    Node(rc => xs.foreach(f => f(rc))) // Apply render context to elements

  /** Converts sequence of T (which can be converted to Node) to document fragment */
  implicit def arbitrarySeqToNode[T](xs: Iterable[T])(implicit ev: T => Node[MiscType]): Node[MiscType] =
    Node(rc => xs.foreach(f => ev(f).apply(rc)))

  /** Implicitly unwraps optional documents */
  implicit def optionToNode(value: Option[Node[MiscType]]): Node[MiscType] =
    Node(rc => if (value.nonEmpty) value.get(rc))

  /** Converts option of T (which can be converted to Node) to document node */
  implicit def arbitraryOptionToNode[T](value: Option[T])(implicit ev: T => Node[MiscType]): Node[MiscType] =
    Node(rc => if (value.nonEmpty) ev(value.get).apply(rc))

  /** Symbol based DSL allows to define documents
    * {{{
    *   'body(
    *     'h1(class /= "title", "Hello World"),
    *     'p("Lorem ipsum dolor")
    *   )
    * }}}
    */
  implicit final class SymbolOps(s: Symbol) {
    def apply(children: Document[MiscType]*): Node[MiscType] =
      macro TemplateDslMacro.node[MiscType]

    def /=(value: String): Attr[MiscType] =
      macro TemplateDslMacro.attr[MiscType]
  }

  @deprecated("Use void instead of <>", since = "0.4.0")
  val <> = Empty

  val void = Empty
}
