import Versions._
import sbt._

object Dependencies {

  // Libraries
  object Compile {
    val jodaTime          = "joda-time"                     %  "joda-time"                          % JodaTimeVersion
    val jodaConvert       = "org.joda"                      %  "joda-convert"                       % JodaConvertVersion
    val elastic4s         = "com.sksamuel.elastic4s"        %% "elastic4s-core"                     % Elastic4sVersion
    val elastic4sJackson  = "com.sksamuel.elastic4s"        %% "elastic4s-jackson"                  % Elastic4sVersion
    val json4s            = "org.json4s"                    %% "json4s-jackson"                     % Json4sVersion
    val json4sExt         = "org.json4s"                    %% "json4s-ext"                         % Json4sVersion
    val akkaHttpJson      = "com.typesafe.akka"             %% "akka-http-spray-json-experimental"  % AkkaVersion
    val akkaActor         = "com.typesafe.akka"             %% "akka-actor"                         % AkkaVersion
    val akkaStream        = "com.typesafe.akka"             %% "akka-stream"                        % AkkaVersion
    val akkaHttp          = "com.typesafe.akka"             %% "akka-http-experimental"             % AkkaVersion
    val guice             = "com.google.inject"             %  "guice"                              % GuiceVersion
    val scalaGuice        = "net.codingwell"                %% "scala-guice"                        % ScalaGuiceVersion
    val ficus             = "com.iheart"                    %% "ficus"                              % FicusVersion
    val slf4jApi          = "org.slf4j"                     %  "slf4j-api"                          % Slf4jVersion
    val logback           = "ch.qos.logback"                %  "logback-classic"                    % LogbackVersion
    val commonsLang       = "org.apache.commons"            %  "commons-lang3"                      % CommonsLangVersion
    val commonsCodec      = "commons-codec"                 %  "commons-codec"                      % CommonsCodecVersion
    val commonsIo         = "commons-io"                    %  "commons-io"                         % CommonsIoVersion
  }

  object Test {
    val akkaTestkit       = "com.typesafe.akka"             %% "akka-testkit"                 % AkkaVersion
    val akkaStreamTestkit = "com.typesafe.akka"             %% "akka-stream-testkit"          % AkkaVersion
    val akkaHttpTestkit   = "com.typesafe.akka"             %% "akka-http-testkit"            % AkkaVersion
    val scalaTest         = "org.scalatest"                 %% "scalatest"                    % ScalaTestVersion
    val scalaMock         = "org.scalamock"                 %% "scalamock-scalatest-support"  % ScalaMockVersion
    val fabricator        = "com.github.azakordonets"       %% "fabricator"                   % FabricatorTestVersion
    val scalaMeterCore    = "com.storm-enroute"             %% "scalameter-core"              % ScalaMeterVersion
    val scalaMeter        = "com.storm-enroute"             %% "scalameter"                   % ScalaMeterVersion
    val elastic4sTestkit  = "com.sksamuel.elastic4s"        %% "elastic4s-testkit"            % Elastic4sVersion
    val groovy            = "org.codehaus.groovy"           %  "groovy-all"                   % GroovyVersion
    val scoverageRuntime  = "org.scoverage"                 %% "scalac-scoverage-runtime"     % ScoverageRuntimeVersion
  }

  import Compile._

  val time              = Seq(jodaConvert, jodaTime)
  val json4sJson        = Seq(json4s, json4sExt)
  val elasticClient     = Seq(elastic4s, elastic4sJackson)
  val akka              = Seq(akkaActor, akkaStream, akkaHttp, akkaHttpJson)
  val di                = Seq(guice, scalaGuice)
  val config            = Seq(ficus)
  val logging           = Seq(slf4jApi, logback)
  val apacheCommons     = Seq(commonsLang, commonsCodec, commonsIo)

  lazy val shared = logging ++ time ++ json4sJson ++ apacheCommons ++ di ++ config ++ Seq(Test.scoverageRuntime)

  // Sub-project specific dependencies
  lazy val testkit = shared ++ elasticClient ++
      Seq(Test.akkaTestkit, Test.akkaStreamTestkit, Test.akkaHttpTestkit, Test.scalaTest, Test.scalaMock,
        Test.elastic4sTestkit, Test.groovy, Test.fabricator, Test.scalaMeterCore, Test.scalaMeter)

  lazy val common = shared ++ Seq(Test.scalaTest % "test") //++ metaProgramming ++ Seq(uuidGenerator)

  //elastic4s dependency has to be first to merge BaseDateTime.class properly
  lazy val peopleApiService = elasticClient ++ shared

  lazy val peopleApiRest = shared ++ akka

  implicit class Exclude(module: ModuleID) {
    def log4jExclude: ModuleID =
      module excludeAll ExclusionRule("log4j")

    def sparkExclusions: ModuleID =
      module.log4jExclude.exclude("org.slf4j", "slf4j-log4j12")
          .exclude("org.eclipse.jetty.orbit", "javax.transaction")
          .exclude("org.eclipse.jetty.orbit", "javax.mail")
          .exclude("org.eclipse.jetty.orbit", "javax.activation")
          .exclude("commons-beanutils", "commons-beanutils-core")
          .exclude("commons-collections", "commons-collections")
          .exclude("commons-collections", "commons-collections")
          .exclude("com.esotericsoftware.minlog", "minlog")

    def kafkaExclusions: ModuleID =
      module.log4jExclude
          .excludeAll(
            ExclusionRule(organization =  "org.slf4j"),
            ExclusionRule(organization =  "javax.jms")
          )
          .exclude("com.sun.jmx", "jmxri")
          .exclude("com.sun.jdmk", "jmxtools")
          .exclude("net.sf.jopt-simple", "jopt-simple")

    def fabricatorExclude: ModuleID =
      module
          .exclude("com.google.inject", "guice")
          .exclude("com.google.guava", "guava")

    def logsExclude: ModuleID =
      module.log4jExclude
          .excludeAll(
            ExclusionRule(organization = "org.slf4j")
          )
          .exclude("org.slf4j", "slf4j-log4j12")

    def awsSdkExclude: ModuleID =
      module.logsExclude
          .exclude("com.fasterxml.jackson.core", "jackson-databind")
  }
}