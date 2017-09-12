package p2

// dependent typing and implicits

import shapeless._
import shapeless.ops.hlist.{IsHCons, Last}

object Chaining {
  def lastField[A, Repr <: HList](input: A)(
                  implicit
                  gen: Generic.Aux[A, Repr],
                  last: Last[Repr]
  ): last.Out = last(gen.to(input))

  def getWrappedValue[A, Repr <: HList, Head](input: A)(
                           implicit
                           gen: Generic.Aux[A, Repr],
                           isHCons: IsHCons.Aux[Repr, Head, HNil]
  ): Head = gen.to(input).head
}

object ChainingApp extends App {
  import Chaining._

  case class Employee(name: String, age: Int, sal: Double)
  case class Hash(hash: String)

  println(lastField(Employee("song", 36, 100)))
  println(getWrappedValue(Hash("Hello")))
}