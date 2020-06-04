addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.5.3")
addSbtPlugin("io.github.alexarchambault.sbt" % "sbt-compatibility" % "0.0.4")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
