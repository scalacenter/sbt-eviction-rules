package sbtevictionrules

import java.util.concurrent.ConcurrentHashMap

import coursier.version.{ModuleMatchers, VersionCompatibility}
import sbt._
import sbt.Keys._
import sbt.librarymanagement.ScalaModuleInfo
import sbt.plugins.JvmPlugin

object EvictionRulesPlugin extends AutoPlugin {

  override def trigger = allRequirements
  override def requires = JvmPlugin

  object autoImport {
    val evictionWarnings = taskKey[Seq[String]]("")
    val evictionCheck = taskKey[Unit]("")
    val evictionIntransitiveCheck = taskKey[Unit]("")
    val evictionRules = settingKey[Seq[ModuleID]]("")
  }
  import autoImport._


  private def pf[A, B](f: A => B): PartialFunction[A, B] = { case x => f(x) }

  private implicit class ModuleIDOps(private val mod: ModuleID) extends AnyVal {
    def actualName(sv: String, sbv: String): String =
      CrossVersion(mod.crossVersion, sv, sbv).fold(mod.name)(_(mod.name))
  }

  private def viaEvictionRules(
    rules: Seq[(ModuleMatchers, VersionCompatibility)],
    sv: String,
    sbv: String
  ): PartialFunction[(ModuleID, Option[ModuleID], Option[ScalaModuleInfo]), Boolean] = {

    val cache = new ConcurrentHashMap[(String, String), Option[VersionCompatibility]]

    def versionCompatibilityOpt(m: ModuleID): Option[VersionCompatibility] = {
      val key = (m.organization, m.actualName(sv, sbv))
      if (!cache.containsKey(key)) {
        val c = rules.collectFirst {
          case (matcher, c0) if matcher.matches(key._1, key._2) =>
            c0
        }
        cache.putIfAbsent(key, c)
      }
      cache.get(key)
    }

    {
      case (m1, Some(m2), _) if versionCompatibilityOpt(m1).nonEmpty =>
        val compat = versionCompatibilityOpt(m1).get
        compat.isCompatible(m1.revision, m2.revision)
    }
  }

  private def rulesSetting: Def.Initialize[Seq[(ModuleMatchers, VersionCompatibility)]] = Def.setting {
    val sv = scalaVersion.value
    val sbv = scalaBinaryVersion.value
    evictionRules.value.map { mod =>
      val name = mod.actualName(sv, sbv)
      val compat = VersionCompatibility(mod.revision).getOrElse {
        sys.error(s"Unrecognized compatibility type: ${mod.revision}")
      }

      ModuleMatchers.only(mod.organization, name) -> compat
    }
  }

  override def buildSettings = Def.settings(
    evictionRules := Seq.empty
  )

  override def projectSettings = Def.settings(

    // like evicted, but only prints warnings
    // adapted from https://github.com/sbt/sbt/blob/b1192c9021970fdd4252498ca5fcf9d7cffa9b32/main/src/main/scala/sbt/Defaults.scala#L2644-L2653
    evictionWarnings := {
      import ShowLines._
      val id = thisProject.value.id
      val report = (Classpaths.updateTask tag (Tags.Update, Tags.Network)).value
      val log = streams.value.log
      val ew =
        EvictionWarning(ivyModule.value, (evictionWarningOptions in evicted).value, report)
      val warnings = ew.lines
      if (warnings.nonEmpty)
        log.warn((s"Found eviction warnings in $id:" +: warnings).mkString(System.lineSeparator))
      warnings
    },

    evictionCheck := Def.taskDyn {
      import sbtevictionrules.internal.Structure._

      val state0 = state.value
      val projectRef = thisProjectRef.value
      val projects = allRecursiveInterDependencies(state0, projectRef)

      val task = evictionIntransitiveCheck.forAllProjects(state0, projectRef +: projects)
      Def.task(task.value)
    }.value,

    evictionIntransitiveCheck := {
      val id = thisProject.value.id
      val warnings = evictionWarnings.value
      if (warnings.nonEmpty)
        sys.error(s"Error, found eviction warnings in $id")
    },

    // evicted implementation that prints project names too
    // adapted from https://github.com/sbt/sbt/blob/b1192c9021970fdd4252498ca5fcf9d7cffa9b32/main/src/main/scala/sbt/Defaults.scala#L2644-L2653
    evicted := {
      import ShowLines._
      val id = thisProject.value.id
      val report = (Classpaths.updateTask tag (Tags.Update, Tags.Network)).value
      val log = streams.value.log
      val ew =
        EvictionWarning(ivyModule.value, (evictionWarningOptions in evicted).value, report)
      val warnings = ew.lines
      val info = sbt.privatesbt.sbtevictionrules.Helper.evictionWarningsInfo(ew)
      if (warnings.nonEmpty)
        log.warn((s"Found eviction warnings in $id:" +: warnings).mkString(System.lineSeparator))
      if (info.nonEmpty)
        log.info((s"Found non problematic eviction(s) in $id:" +: info).mkString(System.lineSeparator))
      ew
    },

    evictionWarningOptions.in(evicted) := {
      val sv = scalaVersion.value
      val sbv = scalaBinaryVersion.value
      val rules = rulesSetting.value
      val previous = evictionWarningOptions.in(evicted).value

      previous.withGuessCompatible(
        viaEvictionRules(rules, sv, sbv)
          .orElse(pf(previous.guessCompatible))
      )
    },
    evictionWarningOptions.in(update) := {
      val sv = scalaVersion.value
      val sbv = scalaBinaryVersion.value
      val rules = rulesSetting.value
      val previous = evictionWarningOptions.in(update).value

      previous.withGuessCompatible(
        viaEvictionRules(rules, sv, sbv)
          .orElse(pf(previous.guessCompatible))
      )
    }
  )

}
