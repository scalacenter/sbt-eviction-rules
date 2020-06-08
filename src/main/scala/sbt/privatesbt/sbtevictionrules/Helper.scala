package sbt.privatesbt.sbtevictionrules

import sbt.librarymanagement.EvictionWarning

object Helper {

  def evictionWarningsInfo(ew: EvictionWarning): List[String] =
    ew.infoAllTheThings

}
