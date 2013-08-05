import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "rested-crowd"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    javaCore
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    publishMavenStyle := true,
    organization := "org.agmip.web.util",
    publishTo    := Some(Resolver.file("file", new File(Path.userHome.absolutePath+"/.m2/repository"))),
    resolvers    += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
  )
}
