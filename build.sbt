
inThisBuild(List(
  organization := "ch.epfl.scala",
  homepage := Some(url("https://github.com/scalacenter/sbt-eviction-rules")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "alexarchambault",
      "Alexandre Archambault",
      "",
      url("https://github.com/alexarchambault")
    )
  )
))

lazy val `sbt-eviction-rules` = project
  .in(file("."))
  .enablePlugins(ScriptedPlugin)
  .aggregate(`sbt-eviction-rules-dummy`)
  .settings(
    sbtPlugin := true,
    scriptedLaunchOpts += "-Dplugin.version=" + version.value,
    scriptedBufferLog := false,
    name := "sbt-eviction-rules",
    libraryDependencies += "io.get-coursier" %% "versions" % "0.2.0",
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
