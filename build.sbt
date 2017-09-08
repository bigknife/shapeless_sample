
scalaVersion in ThisBuild := "2.11.8"
scalacOptions in ThisBuild ++= Seq(
  "-target:jvm-1.7",
  "-encoding",
  "UTF-8",
  "-unchecked",
  "-deprecation",
  "-Xfuture",
  "-feature",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-unused"
)

scalacOptions in (Compile, console) ~= (_ filterNot (_ contains "paradise"))

lazy val shapeless = Seq(
  "com.chuusai" %% "shapeless" % "2.3.2"
)

// common settings
lazy val commonSettings = Seq(
  organization := "ufs3",
  resolvers += Resolver.sonatypeRepo("releases")
)

lazy val shapeless_sample = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= shapeless
  )

