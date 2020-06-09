# sbt-eviction-rules

An sbt plugin enhancing the `evicted` key.

This plugin:
- makes the output of `evicted` slightly more readable
- allows to easily run eviction checks on your CI
- allows to more easily configure evictions that can be ignored.

## How to use

Add to `project/plugins.sbt`:
```scala
addSbtPlugin("io.github.alexarchambault.sbt" % "sbt-eviction-rules" % "0.1.0")
```
The latest version is [![Maven Central](https://img.shields.io/maven-central/v/io.github.alexarchambault.sbt/sbt-eviction-rules-dummy.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.alexarchambault.sbt/sbt-eviction-rules-dummy).

## Better output

### `evicted`

`evicted` prints which of your projects each printed eviction comes from:
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

### `evictionWarnings`

`evictionWarnings` prints only problematic evictions:
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

## Check the absence of problematic evictions

`evictionCheck` checks that there are no problematic evictions, and fails if there are such evictions.
This can be handy to run eviction checks on your CI.

## Easier to configure

If an eviction is printed as problematic, you can configure evictions to loosen
its checks with `evictionRules`, like
```scala
evictionRules += "org.scala-lang.modules" %% "scala-xml" % "semver"
```

This specifies that `"org.scala-lang.modules" %% "scala-xml"` follows
semantic versioning, so that it's fine if version `1.2.0` is selected
where `1.0.6` is expected.

The following compatibility types are available:
- `semver`: assumes the matched modules follow [semantic versioning](https://semver.org),
- `pvp`: assumes the matched modules follow [package versioning policy](https://pvp.haskell.org) (quite common in Scala),
- `always`: assumes all versions of the matched modules are compatible with each other,
- `strict`: requires exact matches between the wanted and the selected versions of the matched modules.

## Module patterns

`evictionRules` accepts `*` as organization or module name, or as parts of them, to match several modules at once:
```scala
evictionRules += "io.get-coursier" %% "*" % "pvp"
evictionRules += "org.typelevel" %% "cats-*" % "semver"
```

## About the default eviction rules in sbt

By default, sbt assumes that
- scala dependencies follow the [package versioning policy](https://pvp.haskell.org),
- other dependencies follow [semantic versioning](https://semver.org).

If any eviction brings an incompatible version per those defaults, sbt warns about it in `update`
and gives more details in `evicted`.

sbt-eviction-rules then allows to ignore the warnings that you think can be safely ignored.

Note that there can be slight discrepancies between the checks
performed by `evictionRules` and those performed by default by sbt:
the checks done by `evictionRules` are handled by the
[coursier versions library](https://github.com/coursier/versions), while those
of sbt are handled by the [`sbt/librarymanagement` library](https://github.com/sbt/librarymanagement).

## Acknowledgments

<img src="https://scala.epfl.ch/resources/img/scala-center-swirl.png" width="40px" />

*sbt-eviction-rules* is funded by the [Scala Center](https://scala.epfl.ch).
