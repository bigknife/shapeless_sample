package migration

import shapeless._, ops.hlist

trait Migration[A, B] {
  def apply(a: A): B
}
object Migration {
  implicit class MigrationOps[A](a: A) {
    def migrateTo[B](implicit migration: Migration[A, B]): B = migration.apply(a)
  }
  /*
  implicit def genericMigration[A, B, ARepr <: HList, BRepr <: HList](
      implicit
      aGen: LabelledGeneric.Aux[A, ARepr],
      bGen: LabelledGeneric.Aux[B, BRepr],
      inter: hlist.Intersection.Aux[ARepr, BRepr, BRepr]
  ): Migration[A, B] = new Migration[A, B] {
    override def apply(a: A): B =
      bGen.from(inter.apply(aGen.to(a)))
  }
  */

  implicit def genericMigration[A, B, ARepr <: HList, BRepr <: HList, Unaligned <: HList](
      implicit
      agen: LabelledGeneric.Aux[A, ARepr],
      bgen: LabelledGeneric.Aux[B, BRepr],
      inter: hlist.Intersection.Aux[ARepr, BRepr, Unaligned],
      align: hlist.Align[Unaligned, BRepr]
  ): Migration[A, B] = new Migration[A, B] {
    override def apply(a: A): B =
      bgen.from(align.apply(inter.apply(agen.to(a))))
  }
}

object MigrationTest extends App {
  import Migration._

  case class IceCreamV1(name: String, numCherries: Int, inCone: Boolean)
  case class IceCreamV2a(name: String, inCone: Boolean)
  case class IceCreamV2b(name: String, inCone: Boolean, numCherries: Int)
  case class IceCreamV2c(name: String, inCone: Boolean, numCherries: Int, numWaffles: Int)

  val v1 = IceCreamV1("Sundae", 1, false)
  println(v1.migrateTo[IceCreamV2a])
  println(v1.migrateTo[IceCreamV2b])
}
