> Note: this project is in maintenance mode because [sbt 1.5.0](https://github.com/sbt/sbt/milestone/68?closed=1)
> provides equivalent features. See [sbt/sbt#6221](https://github.com/sbt/sbt/pull/6221)
> for more details.
> 
> You can still use the version 1.0.0-RC1 of this plugin in case you
> are stuck with an old version of sbt.

# sbt-eviction-rules

An sbt plugin enhancing the `evicted` task.

This plugin:

1. makes the output of `evicted` slightly more readable
2. allows you to easily run eviction checks on your CI
3. allows you to more easily configure evictions that can be ignored
   (to avoid false positive warnings).

Note: sbt has been gradually providing these features. As of sbt
1.5.0, all of the features of this plugin are now supported by sbt
out of the box. Nevertheless, this plugin can be useful if you are
stuck with an old version of sbt.

## Installation

Add to `project/plugins.sbt`:
```scala
addSbtPlugin("ch.epfl.scala" % "sbt-eviction-rules" % "1.0.0-RC1")
```
The latest version is [![Maven Central](https://img.shields.io/maven-central/v/ch.epfl.scala/sbt-eviction-rules-dummy_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/ch.epfl.scala/sbt-eviction-rules-dummy_2.12).

## Usage

The plugin provides the following tasks.

### `evictionWarnings`

Unlike the default `evicted` task, the `evctionWarnings` task reports only problematic
evictions (ie, libraries that have been evicted by binary incompatible versions):

```
> evictionWarnings
[warn] Found eviction warnings in b:
[warn] Found version conflict(s) in library dependencies; some are suspected to be binary incompatible:
[warn]
[warn] 	* org.scala-lang.modules:scala-xml_2.12:1.2.0 is selected over {1.0.6, 1.0.6}
[warn] 	    +- eu.timepit:refined_2.12:0.9.12                     (depends on 1.2.0)
[warn] ct    +- org.scala-lang:scala-compiler:2.12.11              (depends on 1.0.6)
[warn] Found eviction warnings in a:
[warn] Found version conflict(s) in library dependencies; some are suspected to be binary incompatible:
[warn]
[warn] 	* org.scala-lang.modules:scala-xml_2.12:1.2.0 is selected over {1.0.6, 1.0.6}
[warn] 	    +- eu.timepit:refined_2.12:0.9.12                     (depends on 1.2.0)
[warn] 	    +- org.scala-lang:scala-compiler:2.12.11              (depends on 1.0.6)
[success] Total time: 1 s, completed jun 4 2020 16:05:22
```

### `evictionCheck`

This task turns the eviction warnings into errors. It succeeds only if
there are no eviction warnings in your build.

You typically want to invoke this task in your CI, to make sure that no
pull requests introduce eviction warnings.

### `evicted`

The built-in `evicted` task is overridden to provide a more readable output.
It prints which of your projects each printed eviction comes from:

```
> evicted
[warn] Found eviction warnings in b:
[warn] Found version conflict(s) in library dependencies; some are suspected to be binary incompatible:
[warn]
[warn] 	* org.scala-lang.modules:scala-xml_2.12:1.2.0 is selected over {1.0.6, 1.0.6}
[warn] 	    +- eu.timepit:refined_2.12:0.9.12                     (depends on 1.2.0)
[warn] ct    +- org.scala-lang:scala-compiler:2.12.11              (depends on 1.0.6)
[warn] Found eviction warnings in a:
[warn] Found version conflict(s) in library dependencies; some are suspected to be binary incompatible:
[warn]
[warn] 	* org.scala-lang.modules:scala-xml_2.12:1.2.0 is selected over {1.0.6, 1.0.6}
[warn] 	    +- eu.timepit:refined_2.12:0.9.12                     (depends on 1.2.0)
[warn] 	    +- org.scala-lang:scala-compiler:2.12.11              (depends on 1.0.6)
[info] Found non problematic eviction(s) in c:
[info] Here are other dependency conflicts that were resolved:
[info]
[info] 	* org.scala-lang.modules:scala-xml_2.12:1.2.0 is selected over {1.0.6, 1.0.6}
[info] 	    +- eu.timepit:refined_2.12:0.9.12                     (depends on 1.2.0)
[info] 	    +- org.scala-lang:scala-compiler:2.12.11              (depends on 1.0.6)
[success] Total time: 1 s, completed jun 4 2020 15:54:04
```

## Configuration

The [recommended versioning scheme] in the Scala ecosystem is a (stricter) variant
of Semantic Versioning, but not all libraries follow this versioning scheme.

You can configure which versioning scheme is used by which library by using the
`evictionRules` setting:

```scala
evictionRules += "org.scala-lang.modules" %% "scala-xml" % "semver-spec"
```

This specifies that `"org.scala-lang.modules" %% "scala-xml"` follows
semantic versioning, so that it's fine if version `1.2.0` is selected
where `1.0.6` is expected (ie, no evictions will be reported).

The following compatibility types are available:
- `early-semver`: assumes the matched modules follow a variant of [Semantic Versioning](https://semver.org) that enforces compatibility within 0.1.z.
- `semver-spec`: assumes the matched modules follow [Semantic Versioning Spec](https://semver.org) that assumes no compatibility within 0.1.z.
- `pvp`: assumes the matched modules follow [package versioning policy](https://pvp.haskell.org) (quite common in Scala),
- `always`: assumes all versions of the matched modules are compatible with each other,
- `strict`: requires exact matches between the wanted and the selected versions of the matched modules.

> Note that starting with sbt 1.4.x, libraries can embed the versioning
> scheme they use in their artifacts metadata, making the `evictionRules`
> setting unnecessary. This setting is still useful during the transition
> period.

### Module patterns

`evictionRules` accepts `*` as organization or module name, or as parts of them, to match several modules at once:

```scala
evictionRules += "io.get-coursier" %% "*" % "pvp"
evictionRules += "org.typelevel" %% "cats-*" % "semver-spec"
```

## About the default eviction rules in sbt

By default, sbt assumes that
- scala dependencies follow the [package versioning policy](https://pvp.haskell.org),
- other dependencies follow [semantic versioning](https://semver.org).

If any eviction brings an incompatible version per those defaults, sbt warns about it in `update`
and gives more details in `evicted`.

sbt-eviction-rules then allows you to remove false warnings if you know that a library follows
another versioning scheme than PVP.

Note that there can be slight discrepancies between the checks
performed by `evictionRules` and those performed by default by sbt:
the checks done by `evictionRules` are handled by the
[coursier versions library](https://github.com/coursier/versions), while those
of sbt are handled by the [`sbt/librarymanagement` library](https://github.com/sbt/librarymanagement).

## Acknowledgments

<img src="https://scala.epfl.ch/resources/img/scala-center-swirl.png" width="40px" />

*sbt-eviction-rules* is funded by the [Scala Center](https://scala.epfl.ch).

[recommended versioning scheme]: https://docs.scala-lang.org/overviews/core/binary-compatibility-for-library-authors.html#recommended-versioning-scheme
