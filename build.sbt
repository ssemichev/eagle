import Common._
import Settings._
import Tasks._
import sbt.Keys._

name := "eagle"

// ---------------------------------------------------------------------------------------------------------------------
// Root Project
// --------------------------------------------------------------------------------------------------------------------

lazy val publishedProjects = Seq[ProjectReference](
  TestKitProject,
  CommonProject,
  PeopleApiServiceProject,
  PeopleApiRestProject
)

lazy val root = BuildProject("eagle-root", ".")
    .settings(rootSettings: _*)
    .settings(Testing.settings: _*)
    .aggregate(publishedProjects: _*)

// ---------------------------------------------------------------------------------------------------------------------
// Modules
// ---------------------------------------------------------------------------------------------------------------------

lazy val TestKitProject = BuildProject("testkit")
    .settings(
      libraryDependencies ++= Dependencies.testkit
    )
    .dependsOn(CommonProject)

lazy val CommonProject = BuildProject("common")
    .settings(
      libraryDependencies ++= Dependencies.common
    )

lazy val PeopleApiServiceProject = BuildProject("people-api-service", "people-api-service")
    .settings(
      libraryDependencies ++= Dependencies.peopleApiService
    )
    .dependsOn(CommonProject, TestKitProject % "test,it,e2e,bench")

lazy val PeopleApiRestProject: Project = BuildProject("people-api-rest", "people-api-rest")
    .enablePlugins(JavaAppPackaging, DockerPlugin, BuildInfoPlugin, GitVersioning)
    .settings(
      libraryDependencies ++= Dependencies.peopleApiRest,
      dockerExposedPorts := Seq(9000)
    )
    .settings(dockerSettings: _*)
    .settings(releaseSettings: _*)
    .settings(Settings.buildInfoSettings: _*)
    .dependsOn(CommonProject, PeopleApiServiceProject, TestKitProject % "test,it,e2e,bench")

gitHeadCommitSha in ThisBuild := gitHeadCommitShaDef
gitHeadCommitShaShort in ThisBuild := gitHeadCommitShaShortDef
printClassPath in ThisBuild <<= printClassPathDef

addCommandAlias("all", "alias")