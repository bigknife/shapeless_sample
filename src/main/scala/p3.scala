package p3

import shapeless._
import shapeless.labelled._
import shapeless.syntax.singleton._

sealed abstract class Json
final case class JsonObject(fields: List[(String, Json)]) extends Json
final case class JsonArray(items: List[Json])             extends Json
final case class JsonString(value: String)                extends Json
final case class JsonNumber(value: Double)                extends Json
final case class JsonBoolean(value: Boolean)              extends Json
case object JsonNull                                      extends Json

object Json {
  def encode[A](value: A)(implicit encoder: JsonEncoder[A]): Json =
    encoder.encode(value)

  def stringify(json: Json): String = json match {
    case JsonNull           ⇒ "null"
    case JsonBoolean(value) ⇒ value.toString
    case JsonNumber(value)  ⇒ value.toString
    case JsonString(value)  ⇒ "\"" + escape(value) + "\""
    case JsonArray(items)   ⇒ "[" + items.map(stringify).mkString(",") + "]"
    case JsonObject(fields) ⇒ "{" + fields.map(stringifyField).mkString(",") + "}"
  }
  private def stringifyField(field: (String, Json)): String = {
    val (name, value) = field
    escape(name) + ":" + stringify(value)
  }
  private def escape(str: String): String =
    "\"" + str.replaceAll("\"", "\\\\\"") + "\""

}

trait JsonEncoder[A] {
  def encode(value: A): Json
}

trait JsonObjectEncoder[A] extends JsonEncoder[A] {
  override def encode(value: A): JsonObject
}

object JsonEncoder {
  def pure[A](func: A ⇒ Json): JsonEncoder[A] =
    new JsonEncoder[A] {
      override def encode(value: A): Json = func(value)
    }

  def pureObj[A](func: A ⇒ JsonObject): JsonObjectEncoder[A] =
    new JsonObjectEncoder[A] {
      override def encode(value: A): JsonObject = func(value)
    }

  implicit val stringEnc: JsonEncoder[String] =
    pure(str ⇒ JsonString(str))

  implicit val intEnc: JsonEncoder[Int] =
    pure(num => JsonNumber(num))

  implicit val doubleEnc: JsonEncoder[Double] =
    pure(num => JsonNumber(num))

  implicit val booleanEnc: JsonEncoder[Boolean] =
    pure(bool => JsonBoolean(bool))

  implicit val hnilEnc: JsonObjectEncoder[HNil] =
    pureObj(hnil ⇒ JsonObject(Nil))

  implicit def hlistEnc[K <: Symbol, H, T <: HList](
      implicit
      witness: Witness.Aux[K],
      hEnc: Lazy[JsonEncoder[H]],
      tEnc: JsonObjectEncoder[T]): JsonObjectEncoder[FieldType[K, H] :: T] = {
    val fieldName: String = witness.value.name
    pureObj { hlist ⇒
      val head = hEnc.value.encode(hlist.head)
      val tail = tEnc.encode(hlist.tail)
      JsonObject((fieldName, head) :: tail.fields)
    }
  }

  implicit val cnilEnc: JsonObjectEncoder[CNil] = pureObj(cnil ⇒ throw new Exception("inconcivable"))

  implicit def coproductEnc[K <: Symbol, H, T <: Coproduct](
      implicit
      witness: Witness.Aux[K],
      hEnc: Lazy[JsonEncoder[H]],
      tEnc: JsonObjectEncoder[T]): JsonObjectEncoder[FieldType[K, H] :+: T] = {
    val typeName = witness.value.name
    pureObj {
      case Inl(h) ⇒ JsonObject(List(typeName → hEnc.value.encode(h)))
      case Inr(t) ⇒ tEnc.encode(t)
    }
  }

  implicit def genericProductEnc[A, H <: HList](implicit
                                                gen: LabelledGeneric.Aux[A, H],
                                                hEnc: Lazy[JsonObjectEncoder[H]]): JsonEncoder[A] =
    pureObj(x ⇒ hEnc.value.encode(gen.to(x)))
}

final case class Employee(
    name: String,
    number: Int,
    manager: Boolean
)

final case class IceCream(
    name: String,
    numCherries: Int,
    inCone: Boolean
)

sealed trait Shape

final case class Rectangle(
    width: Double,
    height: Double
) extends Shape

final case class Circle(
    radius: Double
) extends Shape

object Main extends App {
  import JsonEncoder._

  val employee1 = Employee("Alice", 1, manager = true)
  val employee2 = Employee("Bob", 2, manager = false)
  val employee3 = Employee("Charlie", 3, manager = false)

  val iceCream1 = IceCream("Cornetto", 0, inCone = true)
  val iceCream2 = IceCream("Sundae", 1, inCone = false)

  val shape1: Shape = Rectangle(3, 4)
  val shape2: Shape = Circle(1)

  //println(Json.stringify(Json.encode(repr)))
  println(Json.encode(employee2))
  println(Json.encode(employee3))
  //println(Json.encode(shape1))
  //println(Json.encode(shape2))
  val repr = LabelledGeneric[Shape].to(shape1)
  println(Json.encode(repr))
  //println(Json.encode(shape2))
}
