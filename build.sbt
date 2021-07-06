ThisBuild / organization := "com.ankit"
ThisBuild / version := "0.2"
ThisBuild / scalaVersion := "2.13.3"

val AkkaVersion = "2.6.13"
val AkkaHttpVersion = "10.2.3"

enablePlugins(JavaAppPackaging)

Compile / mainClass := Some("com.ankit.guild.chat.Main")

lazy val guildChat = (project in file("."))
  .settings(
    name := "guild_chat",
    // processing configs
    libraryDependencies += "com.typesafe" % "config" % "1.4.1",

    // logging
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3",

    // pg jdbc
    libraryDependencies += "org.postgresql" % "postgresql" % "42.2.13",

    // slick
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % "3.3.3",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3"
    ),

    // akka http
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion withSources(),
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion withSources(),
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion withSources(),
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion withSources(),
      "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
    ),

    // akka-http cors extension
    libraryDependencies += "ch.megard" %% "akka-http-cors" % "1.1.1",

    // unit tests
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.9",
      "org.scalatest" %% "scalatest" % "3.2.9" % Test,
    ),
    Test / logBuffered := false,
  )
