version := "0.1.0-SNAPSHOT"
name := "example-http4s-app"
scalaVersion := "2.13.8"

val catsVersion = "2.6.1"
val circeVersion = "0.14.1"
val doobieVersion = "0.13.4"
val h2Version = "1.4.200"
val http4sVersion = "0.21.28"
val kindProjectorVersion = "0.13.2"
val logbackVersion = "1.2.6"
val betterMonadicForVersion = "0.3.1"
val scalaCheckVersion = "1.15.4"
val scalaTestVersion = "3.2.9"
val scalaTestPlusVersion = "3.2.2.0"
val flywayVersion = "7.15.0"
val circeConfigVersion = "0.8.0"
val postgresVersion = "42.2.19"
val oauthJwtVersion   = "3.15.0"
val TsecVersion = "0.2.1"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-literal" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-config" % circeConfigVersion,
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-h2" % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "org.postgresql" %  "postgresql" % postgresVersion,
  "com.h2database" % "h2" % h2Version,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "org.flywaydb" % "flyway-core" % flywayVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion % Test,
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion % Test,
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.scalatestplus" %% "scalacheck-1-14" % scalaTestPlusVersion % Test,
  "io.github.jmcardon" %% "tsec-common" % TsecVersion,
  "io.github.jmcardon" %% "tsec-password" % TsecVersion,
  "io.github.jmcardon" %% "tsec-mac" % TsecVersion,
  "io.github.jmcardon" %% "tsec-signatures" % TsecVersion,
  "io.github.jmcardon" %% "tsec-jwt-mac" % TsecVersion,
  "io.github.jmcardon" %% "tsec-jwt-sig" % TsecVersion,
  "io.github.jmcardon" %% "tsec-http4s" % TsecVersion,
  "com.auth0"               %  "java-jwt"               % oauthJwtVersion,
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % kindProjectorVersion cross CrossVersion.full)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % betterMonadicForVersion)



