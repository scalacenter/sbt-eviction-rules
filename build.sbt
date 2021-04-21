ThisBuild / organization := "ch.epfl.scala"
ThisBuild / homepage := Some(url("https://github.com/scalacenter/sbt-eviction-rules"))
ThisBuild / licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / developers := List(
  Developer(
    "alexarchambault",
    "Alexandre Archambault",
    "",
    url("https://github.com/alexarchambault")
  )
)

lazy val `sbt-eviction-rules` = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .aggregate(`sbt-eviction-rules-dummy`)
  .settings(
    scriptedLaunchOpts += "-Dplugin.version=" + version.value,
    scriptedBufferLog := false,
    name := "sbt-eviction-rules",
    libraryDependencies += "io.get-coursier" %% "versions" % "0.3.1"
  )

lazy val `sbt-eviction-rules-dummy` = project
  .in(file("target/dummy"))
  .disablePlugins(MimaPlugin)
