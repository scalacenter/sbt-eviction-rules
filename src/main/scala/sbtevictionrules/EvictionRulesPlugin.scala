package sbtbetterevicted

import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin
import sbt.librarymanagement.ScalaModuleInfo
import coursier.version.VersionCompatibility
import coursier.version.ModuleMatchers
import java.util.concurrent.ConcurrentHashMap

object EvictionRulesPlugin extends AutoPlugin {

  override def trigger = allRequirements
  override def requires = JvmPlugin

  object autoImport {
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


  override def projectSettings = Def.settings(
    evictionRules := Seq.empty,
    evictionWarningOptions.in(evicted) := {
      val sv = scalaVersion.value
      val sbv = scalaBinaryVersion.value

      val rules = evictionRules.value.map { mod =>
        val name = mod.actualName(sv, sbv)
        val compat = VersionCompatibility(mod.revision).getOrElse {
          sys.error(s"Unrecognized compatibility type: ${mod.revision}")
        }

        ModuleMatchers.only(mod.organization, name) -> compat
      }

      val previous = evictionWarningOptions.in(evicted).value
      previous.withGuessCompatible(
        viaEvictionRules(rules, sv, sbv)
          .orElse(pf(previous.guessCompatible))
      )
    }
  )

}
