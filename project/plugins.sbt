addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.5.7")
addSbtPlugin("ch.epfl.scala" % "sbt-version-policy" % "1.2.1")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
