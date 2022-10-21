addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.11")
addSbtPlugin("ch.epfl.scala" % "sbt-version-policy" % "1.0.1")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
