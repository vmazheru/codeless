lazy val commonSettings = Seq(
  organization := "j2vm",
  version := "0.1.0",
  scalaVersion := "2.11.4",
  crossPaths := false, //remove scala version suffix from porject name
  EclipseKeys.withSource := true,
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
    "junit" % "junit" % "4.11" % "test"
  )
)

lazy val core = project.
  settings(commonSettings: _*).
  settings(
    name := "cl.core"
  )

lazy val jdbc = project.
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    name := "cl.jdbc",
    libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.8.11.1"
  )

lazy val json = project.
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    name := "cl.json",
    libraryDependencies ++= Seq(
      "com.fasterxml.jackson.core" % "jackson-core"        %  "2.5.1",
      "com.fasterxml.jackson.core" % "jackson-databind"    %  "2.5.1",
      "com.fasterxml.jackson.core" % "jackson-annotations" %  "2.5.1"
    )
  )

lazy val ugly = project.
  dependsOn(core).
  settings(commonSettings: _*).
  settings(
    name := "cl.ugly"
  )
