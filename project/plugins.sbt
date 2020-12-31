addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.5.5")
addSbtPlugin("ch.epfl.scala" % "sbt-version-policy" % "1.0.0-RC2")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
