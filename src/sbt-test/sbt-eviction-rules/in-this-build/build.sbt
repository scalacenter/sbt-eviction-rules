
evictionRules.in(ThisBuild) += "org.scala-lang.modules" %% "scala-xml" % "semver"

scalaVersion := "2.12.11"
libraryDependencies ++= Seq(
  "eu.timepit" %% "refined" % "0.9.12",
  "org.scala-lang" % "scala-compiler" % scalaVersion.value
)


lazy val check = taskKey[Unit]("")

check := {
  import ShowLines._
  val evictions = evicted.value.lines
  assert(evictions.isEmpty)
}
