import Common._
import com.scalapenos.sbt.prompt.SbtPrompt.autoImport._
import com.scalapenos.sbt.prompt._
import com.typesafe.sbt.GitPlugin.autoImport.git
import com.typesafe.sbt.SbtNativePackager.autoImport.executableScriptName
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import sbt.Keys._
import sbt._
import Resolvers._
import sbtassembly.AssemblyPlugin.autoImport._
import sbtbuildinfo.BuildInfoPlugin.autoImport._
import sbtassembly.PathList
import sbtrelease.ReleasePlugin.autoImport.ReleaseKeys
import sbtrelease.ReleasePlugin.autoImport.ReleaseStep
import sbtrelease.ReleasePlugin.autoImport.releaseProcess
import sbtrelease.ReleasePlugin.autoImport.releaseUseGlobalVersion
import sbtrelease.Version
import sbtrelease.Versions
import sbtrelease.versionFormatError
import scala.language.postfixOps
import scalariform.formatter.preferences._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations.{setReleaseVersion=>_,_}
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport._
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.stage
import Tasks._

object Settings {

  lazy val buildSettings = Seq(
    organization := "organization",
    description := """API Services""",
    organizationHomepage := Some(url("http://www.<domain>.com")),
    scalaVersion := Versions.ScalaVersion,
    homepage := Some(url("https://github.com/ssemichev/eagle")),
    packageOptions += Package.ManifestAttributes(
      "Implementation-Version" -> (version in ThisBuild).value,
      "Implementation-Title" -> name.value
    ),
    promptTheme := promptThemeValue(version.value),
    updateOptions := updateOptions.value.withCachedResolution(cachedResoluton = true),
    assemblyJarName in assembly := s"eagle-${name.value}_${scalaVersion.value}-assembly-${version.value}.jar",
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = true)
  ) ++ mergeStrategySettings

  val rootSettings = Seq(
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )

  lazy val defaultSettings = testSettings ++ Seq(
    scalacOptions ++= commonScalacOptions,
    javacOptions in Compile ++= commonJavacOptions,
    ivyLoggingLevel in ThisBuild := UpdateLogging.Quiet,
    maxErrors := 20,
    pollInterval := 1000,
    offline := true,
    initialCommands := initialCommandsValue.mkString("\n"),
    initialCommands in console += "//import eagle._",
    initialCommands in(Compile, consoleQuick) <<= initialCommands in Compile,
    resolvers := Seq(TVRepo, Resolver.sonatypeRepo("snapshots"), Resolver.jcenterRepo)
  ) ++ scalariformSettings

  lazy val dockerSettings = Seq(
      dockerBaseImage := "harisekhon/ubuntu-java:jre8",
      dockerRepository := Some(organization.value),
      dockerEntrypoint := Seq("bin/%s" format executableScriptName.value, "-Dconfig.resource=docker.conf"),
      makeDockerVersion := makeDockerVersionTaskImpl.value
  )

  lazy val releaseSettings = Seq(
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      setReleaseVersion,
      runClean,
      runTest,
      tagRelease,
      releaseStepTask(stage in Docker),
      pushChanges
    ),
    git.useGitDescribe := true,
    releaseVersionBump := sbtrelease.Version.Bump.Bugfix,
    releaseVersion <<= releaseVersionBump(bumper => {
      ver => Version(ver)
          .map(_.withoutQualifier)
          .map(_.bump(bumper).string).getOrElse(versionFormatError)
    })
  )

  lazy val buildInfoSettings = Seq(
      buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
      buildInfoPackage := "eagle.common",
      buildInfoOptions ++= Seq(BuildInfoOption.BuildTime, BuildInfoOption.ToJson)
  )

  lazy val testSettings = Seq(
      parallelExecution in Test := false,
      fork in Test := true,
      testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")
    )

  lazy val mergeStrategySettings = Seq(assemblyMergeStrategy in assembly := {
    case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
    case PathList("javax", "transaction", xs@_*) => MergeStrategy.first
    case PathList("javax", "mail", xs@_*) => MergeStrategy.first
    case PathList("javax", "activation", xs@_*) => MergeStrategy.first
    case PathList("org", "apache", xs@_*) => MergeStrategy.last
    case PathList("com", "google", xs@_*) => MergeStrategy.last
    case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.discard
    case PathList("org", "joda", "time", "base", "BaseDateTime.class") => MergeStrategy.first
    case PathList(ps@_*) if ps.last endsWith "pom.properties" => MergeStrategy.first
    case PathList("META-INF", "DEPENDENCIES") => MergeStrategy.first
    case PathList(ps@_*) if ps.last endsWith ".html" => MergeStrategy.first
    case x if x.endsWith("pom.properties") => MergeStrategy.last
    case x if x.endsWith("log4j.properties") => MergeStrategy.last
    case "application.conf" => MergeStrategy.concat
    case "unwanted.txt" => MergeStrategy.discard
    case x =>
      val oldStrategy = (assemblyMergeStrategy in assembly).value
      oldStrategy(x)
  })

  def setVersionOnly(selectVersion: Versions => String): ReleaseStep =  { st: State =>
    val vs = st.get(ReleaseKeys.versions).getOrElse(sys.error("No versions are set! Was this release part executed before inquireVersions?"))
    val selected = selectVersion(vs)

    st.log.info("Setting version to '%s'." format selected)
    val useGlobal =Project.extract(st).get(releaseUseGlobalVersion)
    val versionStr = (if (useGlobal) globalVersionString else versionString) format selected

    reapply(Seq(if (useGlobal) version in ThisBuild := selected else version := selected), st)
  }

  lazy val setReleaseVersion: ReleaseStep = setVersionOnly(_._1)

  private val scalariformSettings = Seq(ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(AlignParameters, true)
      .setPreference(AlignArguments, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(RewriteArrowSymbols, true))

  private val initialCommandsValue = Seq[String](
    """
      |import System.{currentTimeMillis => now}
      |def time[T](f: => T): T = {
      |  val start = now
      |  try { f } finally { println("Elapsed: " + (now - start) + " ms") }
      |}
      | """.stripMargin
  )

  private def promptThemeValue(version: String) = PromptTheme(List(
    text("SBT", NoStyle),
    text(" | ", NoStyle),
    gitBranch(clean = NoStyle, dirty = fg(red)),
    text(s" / $version", NoStyle),
    text(" | ", NoStyle),
    currentProject(NoStyle),
    text(" > ", NoStyle)
  ))
}