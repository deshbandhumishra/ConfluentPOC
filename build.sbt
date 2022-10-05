lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.13.9",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "ConfluentPOC",

    resolvers ++= Seq (
      Opts.resolver.mavenLocalFile,
      "Confluent" at "https://packages.confluent.io/maven"
    )
    ,

    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.4.2",
      "org.apache.kafka" % "kafka-clients" % "3.2.3",
      "org.apache.kafka" % "connect-json" % "3.2.3",
      "org.apache.kafka" % "connect-runtime" % "3.2.3",
      "org.apache.kafka" % "kafka-streams" % "3.2.3",
      "org.apache.kafka" %% "kafka-streams-scala" % "3.2.3",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.4",
      "org.apache.kafka" % "connect-runtime" % "3.2.3",
      "io.confluent" % "kafka-json-serializer" % "5.0.1",
      "javax.ws.rs" % "javax.ws.rs-api" % "2.1.1" artifacts Artifact("javax.ws.rs-api", "jar", "jar") // this is a workaround for https://github.com/jax-rs/api/issues/571
    )

  )