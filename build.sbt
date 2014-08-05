name := "rested-crowd"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaWs
)

publishMavenStyle := true

publishArtifact in Test := false

organization := "org.agmip.web.util"

//publishTo    := Some(Resolver.file("file", new File(Path.userHome.absolutePath+"/.m2/repository"))),
//resolvers    += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <url>http://agmip.org/</url>
  <issueManagement>
    <url>https://github.com/agmip/rested-crowd/issues</url>
    <system>GitHub Issues</system>
  </issueManagement>
  <licenses>
    <license>
      <name>BSD License</name>
      <url>https://raw.github.com/agmip/rested-crowd/master/LICENSE</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>https://github.com/agmip/rested-crowd</url>
    <connection>scm:git:git://github.com/agmip/rested-crowd.git</connection>
    <developerConnection>scm:git:git@github.com:agmip/rested-crowd.git</developerConnection>
    <tag>HEAD</tag>
  </scm>
  <developers>
    <developer>
      <email>cvillalobos@ufl.edu</email>
      <name>Christopher Villalobos</name>
      <url>https://github.com/frostbytten</url>
      <id>frostbytten</id>
    </developer>
  </developers>
)
