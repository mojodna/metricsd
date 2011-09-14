package net.mojodna.metricsd

import com.codahale.logula.Logging
import com.codahale.fig.Configuration
import org.apache.log4j.Level
import java.util.concurrent.TimeUnit
import com.yammer.metrics.reporting.{GraphiteReporter, ConsoleReporter}
import server.MetricsServer

class MetricsDaemon(config: Configuration) extends Logging {
  def apply() = {
    if (config("debug").or(false)) {
      ConsoleReporter.enable(10, TimeUnit.SECONDS)
    }

    val flushInterval = config("graphite.flushInterval").or(10)
    val graphiteHost = config("graphite.host").or("localhost")
    val graphitePort = config("graphite.port").or(2003)
    log.info("Flushing to %s:%d every %ds", graphiteHost, graphitePort, flushInterval)

    GraphiteReporter.enable(
      flushInterval,
      TimeUnit.SECONDS,
      graphiteHost,
      graphitePort
    )

    new MetricsServer(
      config("port").or(MetricsServer.DEFAULT_PORT)
    ).listen
  }
}

object MetricsDaemon {
  def main(args: Array[String]): Unit = {
    val configFile = Option(System.getenv.get("CONFIG"))

    val config = if (configFile == None) {
      System.err.println("Set CONFIG=/path/to/config to use custom settings.")
      new Configuration(scala.io.Source.fromString("{}"))
    } else {
      new Configuration(configFile.get)
    }

    Logging.configure {
      log =>
        log.registerWithJMX = true

        log.level = Level.toLevel(config("log.level").or("TRACE"))

        log.file.enabled = true
        log.file.filename = config("log.file").or("log/development.log")
        log.file.maxSize = 10 * 1024
        log.file.retainedFiles = 5
    }

    new MetricsDaemon(config)()
  }
}