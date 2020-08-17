ThisBuild / version := "0.2.0-SNAPSHOT"
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
    libraryDependencies += "io.get-coursier" %% "versions" % "0.3.0",
    compatibilityIgnoreVersion("0.1.0"),
    mimaPreviousArtifacts := {
      mimaPreviousArtifacts.value.map { mod =>
        if (mod.revision == "0.2.0")
          mod.withOrganization("io.github.alexarchambault.sbt")
        else
          mod
      }
    }
  )

lazy val `sbt-eviction-rules-dummy` = project
  .in(file("target/dummy"))
  .disablePlugins(MimaPlugin)
