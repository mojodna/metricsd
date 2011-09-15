name := "metricsd"

version := "0.0.1"

organization := "net.mojodna"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
    "com.yammer.metrics" % "metrics-core" % "2.0.0-BETA16" withSources(),
    "com.yammer.metrics" % "metrics-graphite" % "2.0.0-BETA16" withSources(),
    "com.yammer.metrics" %% "metrics-scala" % "2.0.0-BETA16" withSources(),
    "org.jboss.netty" % "netty" % "3.2.4.Final" withSources(),
    "com.codahale" %% "jerkson" % "0.4.1",
    "com.codahale" %% "logula" % "2.1.3" withSources(),
    "com.codahale" %% "fig" % "1.1.7" withSources(),
    "org.slf4j" % "slf4j-log4j12" % "1.6.2"
)

resolvers ++= Seq(
    "Coda Hale's Repository" at "http://repo.codahale.com/",
    "JBoss Repo" at "https://repository.jboss.org/nexus/content/repositories/releases"
)

seq(sbtassembly.Plugin.assemblySettings: _*)
