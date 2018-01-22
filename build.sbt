// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `demo-akka-cluster` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin, GitVersioning, DockerPlugin, JavaAppPackaging)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaClusterShardingTyped,
        library.akkaManagementClusterBootstrap,
        library.akkaManagementClusterHttp,
        library.akkaDiscoveryK8s,
        library.akkaHttp,
        library.akkaHttpCirce,
        library.akkaLog4j,
        library.akkaStream,
        library.circeGeneric,
        library.disruptor,
        library.log4jApiScala,
        library.log4jCore,
        library.pureConfig,
        library.scalapbRuntime % "protobuf"
      )
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val akka           = "2.5.9"
      val akkaHttp       = "10.0.11"
      val akkaHttpJson   = "1.19.0"
      val akkaLog4j      = "1.6.0"
      val akkaManagement = "0.9.0"
      val circe          = "0.9.0"
      val disruptor      = "3.3.7"
      val log4j          = "2.10.0"
      val log4jApiScala  = "11.0"
      val pureConfig     = "0.9.0"
      val scalaCheck     = "1.13.5"
      val scalapb        = com.trueaccord.scalapb.compiler.Version.scalapbVersion
      val utest          = "0.6.3"
    }
    val akkaClusterShardingTyped       = "com.typesafe.akka"             %% "akka-cluster-sharding-typed"       % Version.akka
    val akkaManagementClusterBootstrap = "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % Version.akkaManagement
    val akkaManagementClusterHttp      = "com.lightbend.akka.management" %% "akka-management-cluster-http"      % Version.akkaManagement
    val akkaDiscoveryDns               = "com.lightbend.akka.discovery"  %% "akka-discovery-dns"                % Version.akkaManagement
    val akkaDiscoveryK8s               = "com.lightbend.akka.discovery"  %% "akka-discovery-kubernetes-api"     % Version.akkaManagement
    val akkaHttp                       = "com.typesafe.akka"             %% "akka-http"                         % Version.akkaHttp
    val akkaHttpCirce                  = "de.heikoseeberger"             %% "akka-http-circe"                   % Version.akkaHttpJson
    val akkaLog4j                      = "de.heikoseeberger"             %% "akka-log4j"                        % Version.akkaLog4j
    val akkaStream                     = "com.typesafe.akka"             %% "akka-stream"                       % Version.akka
    val circeGeneric                   = "io.circe"                      %% "circe-generic"                     % Version.circe
    val disruptor                      = "com.lmax"                      %  "disruptor"                         % Version.disruptor
    val log4jApiScala                  = "org.apache.logging.log4j"      %% "log4j-api-scala"                   % Version.log4jApiScala
    val log4jCore                      = "org.apache.logging.log4j"      %  "log4j-core"                        % Version.log4j
    val pureConfig                     = "com.github.pureconfig"         %% "pureconfig"                        % Version.pureConfig
    val scalaCheck                     = "org.scalacheck"                %% "scalacheck"                        % Version.scalaCheck
    val scalapbRuntime                 = "com.trueaccord.scalapb"        %% "scalapb-runtime"                   % Version.scalapb
    val utest                          = "com.lihaoyi"                   %% "utest"                             % Version.utest
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
  gitSettings ++
  scalafmtSettings ++
  dockerSettings ++
  commandAliases

lazy val commonSettings =
  Seq(
    // scalaVersion from .travis.yml via sbt-travisci
    // scalaVersion := "2.12.4",
    organization := "com.mh",
    organizationName := "mh",
    startYear := Some(2018),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-target:jvm-1.8",
      "-encoding", "UTF-8"
    ),
    Compile / unmanagedSourceDirectories := Seq((Compile / scalaSource).value),
    Test / unmanagedSourceDirectories := Seq((Test / scalaSource).value),
    Compile / packageDoc / publishArtifact := false,
    Compile / packageSrc / publishArtifact := false,
    testFrameworks += new TestFramework("utest.runner.Framework")
)

lazy val gitSettings =
  Seq(
    git.useGitDescribe := true
  )

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )

lazy val dockerSettings =
  Seq(
    Docker / daemonUser := "root",
    Docker / maintainer := "delasoul",
    Docker / version := "latest",
    dockerBaseImage := "openjdk:8u151-slim",
    dockerExposedPorts := Vector(8000),
    dockerRepository := Some("delasoul")
  )

lazy val commandAliases =
  addCommandAlias(
    "r0",
    """|reStart
       |---
       |-Dakka.management.http.port=20000
       |-Dakka.remote.artery.canonical.hostname=127.0.0.1
       |-Dakka.remote.artery.canonical.port=10000
       |-Dakka.cluster.seed-nodes.0=akka://dac@127.0.0.1:10000""".stripMargin
  ) ++
  addCommandAlias(
    "r1",
    """|reStart
       |---
       |-Ddac.api.port=8001
       |-Dakka.management.http.port=20001
       |-Dakka.remote.artery.canonical.hostname=127.0.0.1
       |-Dakka.remote.artery.canonical.port=10001
       |-Dakka.cluster.seed-nodes.0=akka://dac@127.0.0.1:10000""".stripMargin
  )
