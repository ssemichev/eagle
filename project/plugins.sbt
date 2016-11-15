logLevel := Level.Warn

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

//https://github.com/sbt/sbt-git/issues/102
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")
addSbtPlugin("com.scalapenos" % "sbt-prompt" % "0.2.1")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.4.0")

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.3.4")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.8.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.0-M5")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.3")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.2.0")