// Scala versions
val scala213 = "2.13.5"
val scala212 = "2.12.13"
val scala211 = "2.11.12"
val scala3 = "3.0.0-RC1"
val scala2 = List(scala213, scala212, scala211)
val allScalaVersions = scala3 :: scala2
val scalaJVMVersions = allScalaVersions
val scalaJSVersions = allScalaVersions
val scalaNativeVersions = scala2
val compatScalaVersions = List(scala213, scala212)

name := "scribe"
organization in ThisBuild := "com.outr"
version in ThisBuild := "3.5.1"
scalaVersion in ThisBuild := scala213
scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")
javacOptions in ThisBuild ++= Seq("-source", "1.8", "-target", "1.8")
resolvers in ThisBuild += Resolver.sonatypeRepo("releases")
resolvers in ThisBuild += Resolver.sonatypeRepo("snapshots")
resolvers in ThisBuild += Resolver.JCenterRepository
//javaOptions in run += "-agentpath:/opt/YourKit-JavaProfiler-2020.9/bin/linux-x86-64/libyjpagent.so=delay=10000,listen=all"

publishTo in ThisBuild := sonatypePublishTo.value
sonatypeProfileName in ThisBuild := "com.outr"
licenses in ThisBuild := Seq("MIT" -> url("https://github.com/outr/scribe/blob/master/LICENSE"))
sonatypeProjectHosting in ThisBuild := Some(xerial.sbt.Sonatype.GitHubHosting("outr", "scribe", "matt@outr.com"))
homepage in ThisBuild := Some(url("https://github.com/outr/scribe"))
scmInfo in ThisBuild := Some(
  ScmInfo(
    url("https://github.com/outr/scribe"),
    "scm:git@github.com:outr/scribe.git"
  )
)
developers in ThisBuild := List(
  Developer(id="darkfrog", name="Matt Hicks", email="matt@matthicks.com", url=url("http://matthicks.com"))
)
parallelExecution in ThisBuild := false

// Core
val perfolationVersion: String = "1.2.5"
val sourcecodeVersion: String = "0.2.4"
val collectionCompatVersion: String = "2.4.3"
val moduloadVersion: String = "1.1.2"

// JSON
val fabricVersion: String = "1.0.2"

// Testing
val testyVersion: String = "1.0.1"

// SLF4J
val slf4jVersion: String = "1.7.30"
val slf4j18Version: String = "1.8.0-beta4"

// Config Dependencies
val profigVersion: String = "3.2.1"

// Slack and Logstash Dependencies
val youiVersion: String = "0.13.20"

// Benchmarking Dependencies
val log4jVersion: String = "2.13.3"
val disruptorVersion: String = "3.4.2"
val logbackVersion: String = "1.2.3"
val typesafeConfigVersion: String = "1.4.0"
val scalaLoggingVersion: String = "3.9.2"
val tinyLogVersion: String = "1.3.6"
val log4sVersion: String = "1.8.2"

// set source map paths from local directories to github path
val sourceMapSettings = List(
  scalacOptions ++= git.gitHeadCommit.value.map { headCommit =>
    val local = baseDirectory.value.toURI
    val remote = s"https://raw.githubusercontent.com/outr/scribe/$headCommit/"
    s"-P:scalajs:mapSourceURI:$local->$remote"
  }
)

lazy val root = project.in(file("."))
  .aggregate(
    coreJS, coreJVM, coreNative,
    fileJVM, fileNative,
    jsonJS, jsonJVM,
    slf4j, slf4j18, migration, config, slack, logstash)
  .settings(
    name := "scribe",
    publish := {},
    publishLocal := {}
  )

lazy val core = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .settings(
    name := "scribe",
    libraryDependencies ++= Seq(
      "com.outr" %%% "perfolation" % perfolationVersion,
      "com.outr" %%% "sourcecode" % sourcecodeVersion,
      "com.outr" %%% "testy" % testyVersion % Test
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    libraryDependencies ++= (
      if (scalaVersion.value.startsWith("3.0")) {
        Nil
      } else {
        List("org.scala-lang.modules" %% "scala-collection-compat" % collectionCompatVersion)
      }
    ),
    publishArtifact in Test := false
  )
  .jsSettings(
    crossScalaVersions := scalaJSVersions,
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )
  .jvmSettings(
    crossScalaVersions := scalaJVMVersions,
    libraryDependencies ++= Seq(
      "com.outr" %% "moduload" % moduloadVersion
    )
  )
  .nativeSettings(
    crossScalaVersions := scalaNativeVersions
  )

lazy val coreJS = core.js
lazy val coreJVM = core.jvm
lazy val coreNative = core.native

lazy val fileModule = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .settings(
    name := "scribe-file",
    libraryDependencies ++= Seq(
      "com.outr" %%% "testy" % testyVersion % Test
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )
  .jvmSettings(
    crossScalaVersions := scalaJVMVersions
  )
  .nativeSettings(
    crossScalaVersions := scalaNativeVersions,
    nativeLinkStubs := true,
    test := {}
  )
  .dependsOn(core)

lazy val fileJVM = fileModule.jvm
lazy val fileNative = fileModule.native

lazy val json = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .settings(
    name := "scribe-json",
    libraryDependencies ++= Seq(
      "com.outr" %%% "fabric-parse" % fabricVersion,
      "com.outr" %%% "testy" % testyVersion % Test
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    crossScalaVersions := List(scala213, scala212)
  )
  .jsSettings(
    crossScalaVersions := scalaJSVersions,
    Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )
  .jvmSettings(
    crossScalaVersions := scalaJVMVersions
  )
  .dependsOn(core)

lazy val jsonJS = json.js
lazy val jsonJVM = json.jvm

lazy val slf4j = project.in(file("slf4j"))
  .dependsOn(coreJVM)
  .settings(
    name := "scribe-slf4j",
    publishArtifact in Test := false,
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "com.outr" %% "testy" % testyVersion % Test
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    crossScalaVersions := scalaJVMVersions
  )

lazy val slf4j18 = project.in(file("slf4j18"))
  .dependsOn(coreJVM)
  .settings(
    name := "scribe-slf4j18",
    publishArtifact in Test := false,
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % slf4j18Version,
      "com.outr" %% "testy" % testyVersion % Test
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    crossScalaVersions := scalaJVMVersions
  )

lazy val migration = project.in(file("migration"))
  .dependsOn(coreJVM)
  .settings(
    name := "scribe-migration",
    publishArtifact in Test := false,
    libraryDependencies ++= Seq(
      "com.outr" %% "testy" % testyVersion % Test
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    crossScalaVersions := scalaJVMVersions
  )

lazy val config = project.in(file("config"))
  .dependsOn(migration)
  .settings(
    name := "scribe-config",
    publishArtifact in Test := false,
    libraryDependencies ++= Seq(
      "com.outr" %% "profig" % profigVersion,
      "com.outr" %% "testy" % testyVersion % Test
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    crossScalaVersions := compatScalaVersions
  )

lazy val slack = project.in(file("slack"))
  .settings(
    name := "scribe-slack",
    crossScalaVersions := compatScalaVersions,
    libraryDependencies ++= Seq(
      "io.youi" %% "youi-client" % youiVersion
    )
  )
  .dependsOn(coreJVM)

lazy val logstash = project.in(file("logstash"))
  .settings(
    name := "scribe-logstash",
    crossScalaVersions := compatScalaVersions,
    libraryDependencies ++= Seq(
      "io.youi" %% "youi-client" % youiVersion
    )
  )
  .dependsOn(coreJVM)

lazy val benchmarks = project.in(file("benchmarks"))
  .dependsOn(fileJVM)
  .enablePlugins(JmhPlugin)
  .settings(
    publishArtifact := false,
    libraryDependencies ++= Seq(
      "org.apache.logging.log4j" % "log4j-api" % log4jVersion,
      "org.apache.logging.log4j" % "log4j-core" % log4jVersion,
      "com.lmax" % "disruptor" % disruptorVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "com.typesafe" % "config" % typesafeConfigVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
      "org.tinylog" % "tinylog" % tinyLogVersion,
      "org.log4s" %% "log4s" % log4sVersion
    )
  )