
inThisBuild(List(
  organization := "io.github.alexarchambault.sbt",
  homepage := Some(url("https://github.com/alexarchambault/sbt-eviction-rules")),
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
    sonatypeProfileName := "io.github.alexarchambault",
    compatibilityIgnoreVersion("0.1.0")
  )

lazy val `sbt-eviction-rules-dummy` = project
  .in(file("target/dummy"))
  .settings(
    sonatypeProfileName := "io.github.alexarchambault"
  )
