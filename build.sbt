// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `demo-akka-cluster` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin, GitVersioning, DockerPlugin, JavaAppPackaging)
    .settings(settings)
    .settings(
      bashScriptExtraDefines ++= IO.readLines(baseDirectory.value / "native_packager_parameter.sh"),
      javaOptions in Universal ++= globalJavaOptions,
      libraryDependencies ++= Seq(
        library.akkaClusterShardingTyped,
        library.akkaManagementClusterBootstrap,
        library.akkaManagementClusterHttp,
        library.akkaDiscoveryK8s,
        //library.akkaDiscoveryDns,
        library.akkaHttp,
        library.akkaHttpCirce,
        library.akkaLog4j,
        library.akkaStream,
        library.akkaClusterDowning,
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
      val akka                = "2.5.17"
      val akkaHttp            = "10.1.5"
      val akkaHttpCirce       = "1.21.0"
      val akkaLog4j           = "1.6.1"
      val akkaManagement      = "0.18.0"
      val akkaClusterDowning  = "0.0.14-SNAPSHOT"
      val circeGeneric        = "0.9.3"
      val disruptor           = "3.4.2"
      val log4j               = "2.11.0"
      val log4jApiScala       = "11.0"
      val pureConfig          = "0.10.0"
      val scalaCheck          = "1.13.5"
      val scalapb             = com.trueaccord.scalapb.compiler.Version.scalapbVersion
      val utest               = "0.6.3"
    }
    val akkaClusterShardingTyped       = "com.typesafe.akka"             %% "akka-cluster-sharding-typed"       % Version.akka
    val akkaManagementClusterBootstrap = "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % Version.akkaManagement
    val akkaManagementClusterHttp      = "com.lightbend.akka.management" %% "akka-management-cluster-http"      % Version.akkaManagement
    val akkaDiscoveryDns               = "com.lightbend.akka.discovery"  %% "akka-discovery-dns"                % Version.akkaManagement
    val akkaDiscoveryK8s               = "com.lightbend.akka.discovery"  %% "akka-discovery-kubernetes-api"     % Version.akkaManagement
    val akkaHttp                       = "com.typesafe.akka"             %% "akka-http"                         % Version.akkaHttp
    val akkaHttpCirce                  = "de.heikoseeberger"             %% "akka-http-circe"                   % Version.akkaHttpCirce
    val akkaLog4j                      = "de.heikoseeberger"             %% "akka-log4j"                        % Version.akkaLog4j
    val akkaStream                     = "com.typesafe.akka"             %% "akka-stream"                       % Version.akka
    val akkaClusterDowning             = "com.github.TanUkkii007"        %% "akka-cluster-custom-downing"       % Version.akkaClusterDowning
    val circeGeneric                   = "io.circe"                      %% "circe-generic"                     % Version.circeGeneric
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
  sbtSettings ++
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

// Java Options set via sbt have to be added explicitly to native packager
// Maintain the list here and reference in relevant scopes.
lazy val globalJavaOptions = Seq(
  // See https://logging.apache.org/log4j/2.x/manual/async.html for using async logger
  "-Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector",
  // This service is headless.
  "-Djava.awt.headless=true"
)

lazy val sbtSettings =
  Seq(
    fork := true,
    cancelable in Global := true
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
