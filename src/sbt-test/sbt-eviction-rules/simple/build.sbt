
lazy val check = taskKey[Unit]("")

lazy val noEvictions = Def.task {
  import ShowLines._
  val evictions = evicted.value.lines
  assert(evictions.isEmpty)
}

lazy val hasEvictions = Def.task {
  import ShowLines._
  val evictions = evicted.value.lines
  assert(evictions.nonEmpty)
}

lazy val shared = Def.settings(
  scalaVersion := "2.12.11",
  libraryDependencies ++= Seq(
    "eu.timepit" %% "refined" % "0.9.12",
    "org.scala-lang" % "scala-compiler" % scalaVersion.value
  )
)

lazy val a = project
  .settings(
    shared,
    check := hasEvictions.value
  )

lazy val b = project
  .settings(
    shared,
    evictionRules += "org.scala-lang.modules" %% "scala-xml" % "pvp",
    check := hasEvictions.value
  )

lazy val c = project
  .settings(
    shared,
    evictionRules += "org.scala-lang.modules" %% "scala-xml" % "semver",
    check := noEvictions.value
  )
