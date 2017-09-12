package p1
import shapeless._

// type class

trait CsvEncoder[A] {
  def encode(value: A): List[String]
}

object CsvEncoder {
  // summoner
  def apply[A](implicit inst: CsvEncoder[A]): CsvEncoder[A] = inst

  // constructor
  def instance[A](f: A ⇒ List[String]): CsvEncoder[A] = new CsvEncoder[A] {
    override def encode(value: A): List[String] = f(value)
  }

  implicit def intInstance: CsvEncoder[Int]       = instance(x ⇒ List(x.toString))
  implicit def stringInstance: CsvEncoder[String] = instance(x ⇒ List(x))
  implicit def doubleInstance: CsvEncoder[Double] =
    instance(d ⇒ List(d.toString))
  implicit def boolInstance: CsvEncoder[Boolean] =
    instance(b ⇒ List(if (b) "yes" else "no"))

  implicit def hnilInstance: CsvEncoder[HNil] = instance(_ ⇒ Nil)
  implicit def hlistInstance[H, T <: HList](
      implicit
      hInstance: Lazy[CsvEncoder[H]],
      tInstance: CsvEncoder[T]
  ): CsvEncoder[H :: T] =
    instance {
      case h :: t ⇒ hInstance.value.encode(h) ++ tInstance.encode(t)
    }

  implicit def cnilInstance: CsvEncoder[CNil] = instance(_ ⇒ throw new Exception("impossible"))
  implicit def coproductInstance[H, T <: Coproduct](
      implicit
      hInstance: Lazy[CsvEncoder[H]],
      tInstance: CsvEncoder[T]
  ): CsvEncoder[H :+: T] = instance {
    case Inl(a) ⇒ hInstance.value.encode(a)
    case Inr(a) ⇒ tInstance.encode(a)
  }

  implicit def geneircInstance[A, R](
      implicit
      generic: Generic.Aux[A, R],
      rInstance: Lazy[CsvEncoder[R]]
  ): CsvEncoder[A] = instance(a ⇒ rInstance.value.encode(generic.to(a)))
}

object CsvEncoderApp extends App {
  case class Employee(name: String, age: Int)

  sealed trait Shape
  case class Rectangle(width: Double, height: Double) extends Shape
  case class Circle(radius: Double) extends Shape

  println(implicitly[CsvEncoder[Employee]])
  println(implicitly[CsvEncoder[Shape]])

  val rectangle = Rectangle(3.0, 4.0)
  val circle = Circle(2.0)

  println(CsvEncoder[Shape].encode(rectangle))
  println(CsvEncoder[Shape].encode(circle))
}
