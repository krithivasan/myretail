import io.ino.sbtpillar.Plugin.PillarKeys._
import sbt.dsl.enablePlugins
import sbtassembly.MergeStrategy
import sbt.Keys._
import io.gatling.sbt.GatlingPlugin

lazy val akkaHttpVersion = "10.0.11"
lazy val akkaVersion    = "2.5.11"
lazy val cassandraDriverV = "3.3.2"
lazy val akkaSwaggerV = "0.11.2"
lazy val swaggerScalaV = "1.0.4"
lazy val cassandraUnitV = "3.3.0.2"
enablePlugins(DockerPlugin)
enablePlugins(DockerComposePlugin)
enablePlugins(GatlingPlugin)

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.myretail",
      scalaVersion    := "2.12.4"
    )),
    name := "myRetail-REST-API",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "com.datastax.cassandra" % "cassandra-driver-core" % cassandraDriverV exclude("org.xerial.snappy", "snappy-java"),
      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.1"         % Test,
      "com.typesafe.play" %% "play-json" % "2.6.0",
      "com.github.swagger-akka-http" %% "swagger-akka-http" % akkaSwaggerV,
      "io.swagger" % "swagger-scala-module_2.12" % swaggerScalaV,
      "org.cassandraunit" % "cassandra-unit" % cassandraUnitV % Test,
      "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.3.0" % Test,
      "io.gatling"            % "gatling-test-framework"    % "2.3.0" % Test
    )

  )

val delayStep = taskKey[Unit]("delay task")
delayStep := {
  Thread.sleep(10000)
}

addCommandAlias("demo", "; dockerComposeUp; delayStep; createKeyspace; migrate")

test in assembly := {}

assemblyMergeStrategy in assembly  := {
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

//********************************************************
// Docker settings
//********************************************************

dockerfile in docker := {
  val artifact: File = assembly.value
  val artifactTargetPath = s"/${artifact.name}"

  new Dockerfile {
    from("frolvlad/alpine-scala")
    add(artifact, artifactTargetPath)
    expose(8080)
    entryPoint("java", "-jar", artifactTargetPath)
  }
}

//********************************************************
// docker-compose settings
//********************************************************

imageNames in docker := Seq(
  ImageName(s"${organization.value}/${name.value}:latest".toLowerCase)
)

dockerImageCreationTask := docker.value
composeFile := baseDirectory.value + "/docker/docker-compose.yml"
composeServiceName := "myretail"
composeContainerPauseBeforeTestSeconds := 30

//********************************************************
// Scoverage settings
//********************************************************
coverageEnabled in Test := true

coverageMinimum := 50

coverageFailOnMinimum := true


//********************************************************
// SBT Pillar Plugin Settings for Cassandra migration
//********************************************************
pillarSettings

pillarConfigFile := file("conf/pillar.conf")

pillarConfigKey := "cassandra.url"

pillarDefaultConsistencyLevelConfigKey := "cassandra.defaultConsistencyLevel"

pillarMigrationsDir := file("conf/migrations")
