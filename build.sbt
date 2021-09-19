name := "recipe-crawler"

version := "0.1"

scalaVersion := "2.13.5"
// PROJECTS

lazy val global = project
  .in(file("."))
  .settings(settings)
  .disablePlugins(AssemblyPlugin)
  .aggregate(
    common,
    master,
    crawler
  )

lazy val common = project
  .settings(
    name := "common",
    settings,
    libraryDependencies ++= commonDependencies
  )
  .disablePlugins(AssemblyPlugin)

lazy val master = project
  .settings(
    name := "master",
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
    )
  )
  .dependsOn(
    common
  )

lazy val crawler = project
  .settings(
    name := "crawler",
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq(
    )
  )
  .dependsOn(
    common
  )

// DEPENDENCIES

lazy val dependencies =
  new {
    val zioV            = "2.0.0-M2"
    val zioConfigV      = "1.0.6"
    val scalatestV      = "3.0.4"
    val scalacheckV     = "1.13.5"
    val redisV          = "3.40"

    val zio             = "dev.zio"                     %% "zio"                        % zioV
    val zioConfig       = "dev.zio"                     %% "zio-config-typesafe"        % zioConfigV
    val redis           = "net.debasishg"               %% "redisclient"                % redisV
    val scalatest       = "org.scalatest"               %% "scalatest"                  % scalatestV
    val scalacheck      = "org.scalacheck"              %% "scalacheck"                 % scalacheckV
  }

lazy val commonDependencies = Seq(
  dependencies.zio,
  dependencies.zioConfig,
  dependencies.redis,
  dependencies.scalatest  % "test",
  dependencies.scalacheck % "test"
)

// SETTINGS

lazy val settings =
  commonSettings

lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-deprecation",
  "-encoding",
  "utf8"
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
  )
)


lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case "application.conf"            => MergeStrategy.concat
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  }
)