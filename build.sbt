lazy val commonSettings = Seq(
  organization := "j2vm",
  version := "0.1.0",
  scalaVersion := "2.11.4",
  crossPaths := false, //remove scala version suffix from porject name
  EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
  EclipseKeys.withSource := true,
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
    "junit" % "junit" % "4.12" % "test",
    "com.novocode" % "junit-interface" % "0.11" % "test"
  ),
  testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v", "-a")
)

lazy val core = project.
  settings(commonSettings: _*).
  settings(
    name := "cl.core"
  )

lazy val util = project.
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    name := "cl.util"
  ) 

lazy val jdbc = project.
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    name := "cl.jdbc",
    libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.8.11.1"
  )

lazy val cfg = project.
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    name := "cl.cfg"
  )

lazy val json = project.
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    name := "cl.json",
    libraryDependencies ++= Seq(
      "com.fasterxml.jackson.core"     % "jackson-core"            %  "2.5.1",
      "com.fasterxml.jackson.core"     % "jackson-databind"        %  "2.5.1",
      "com.fasterxml.jackson.core"     % "jackson-annotations"     %  "2.5.1",
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" %  "2.5.1"
    )
  )

lazy val logging = project.
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    name := "cl.logging"
  )

lazy val logging_log4j = project.
  dependsOn(logging).
  settings(commonSettings: _*).
  settings(
    name := "cl.logging.log4j",
    libraryDependencies ++= Seq(
      "log4j" % "log4j" % "1.2.17"
    )
  )

lazy val files = project.
  dependsOn(core).
  dependsOn(json).
  settings(commonSettings: _*).
  settings(
    name := "cl.files"
  )

lazy val ugly = project.
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    name := "cl.ugly"
  )
