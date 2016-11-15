import sbt._

object Resolvers {
  lazy val TVRepo = "Internal Repository" at "https://s3.amazonaws.com/artifacts.<domain>.us/maven-s3/releases"
}