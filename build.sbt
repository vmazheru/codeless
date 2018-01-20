lazy val commonSettings = Seq(
  organization := "vmazheru.codeless",
  version := "0.9.0",
  scalaVersion := "2.11.8",
  crossPaths := false, //remove scala version suffix from porject name
  javacOptions in (Compile, compile) ++= Seq("-source", "1.8", "-target", "1.8"),
  EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
  EclipseKeys.withSource := true,
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
    ),
    javacOptions in (Compile,doc) ++= Seq("-package")
  )

lazy val serializers = project.
  dependsOn(json).
  settings(commonSettings: _*).
  settings(
    name := "cl.serializers"
  )

lazy val util = project.
  dependsOn(serializers % "compile->compile;test->test").
  settings(commonSettings: _*).
  settings(
    name := "cl.util"
  )
