package metricsd.server

import scala.math.round
import com.codahale.logula.Logging
import org.jboss.netty.channel.{ExceptionEvent, MessageEvent, ChannelHandlerContext, SimpleChannelUpstreamHandler}
import com.yammer.metrics.core.MetricName
import java.util.concurrent.TimeUnit
import com.yammer.metrics.Metrics

/**
 * A service handler for :-delimited metrics (à la Etsy's statsd).
 */
class MetricsServiceHandler
  extends SimpleChannelUpstreamHandler with Logging {

  val COUNTER_METRIC_TYPE = "c"
  val HISTOGRAM_METRIC_TYPE = "h"
  val METER_METRIC_TYPE = "m"
  val TIMER_METRIC_TYPE = "ms"

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) = {
    val msg = e.getMessage.asInstanceOf[String]

    log.trace("Received message: %s", msg)

    // parse message and do stuff
    val messageParts = msg.split(":")
    val metricName = messageParts(0).trim.replaceAll("\\s+", "_").replaceAll("\\/", "-").replaceAll("[^a-zA-Z_\\-0-9\\.]", "")

    messageParts.length match {
      case 1 =>
        // TODO remove redundancy
        log.trace("Marking meter '%s'", metricName)
        Metrics.newMeter(new MetricName("metrics", "meter", metricName), "samples", TimeUnit.SECONDS).mark

      case _ =>
        val valueParts = messageParts(1).trim.split("\\|")
        val value = valueParts(0).toLong

        // extract the metric type
        val metricType = if (valueParts.length > 1) {
          valueParts(1)
        } else {
          COUNTER_METRIC_TYPE
        }

        metricType match {
          case COUNTER_METRIC_TYPE =>
            // extract the sample rate
            val sampleRate = if (valueParts.length > 2) {
              valueParts(2).toFloat
            } else {
              1.0
            }

            log.trace("Incrementing counter '%s' with %d at sample rate %f (%d)", metricName, value, sampleRate, round(value * 1 / sampleRate))
            Metrics.newCounter(new MetricName("metrics", "counter", metricName)).inc(round(value * 1 / sampleRate))

          case HISTOGRAM_METRIC_TYPE | TIMER_METRIC_TYPE =>
            log.trace("Updating histogram '%s' with %d", metricName, value)
            // note: assumes that values have been normalized to integers
            Metrics.newHistogram(new MetricName("metrics", "histogram", metricName)).update(value)

          case METER_METRIC_TYPE =>
            log.trace("Marking meter '%s'", metricName)
            Metrics.newMeter(new MetricName("metrics", "meter", metricName), "samples", TimeUnit.SECONDS).mark

          case x: String =>
            log.error("Unknown metric type: %s", x)
        }
    }

    Metrics.newMeter(new MetricName("metricsd", "meter", "samples"), "samples", TimeUnit.SECONDS).mark
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) = {
    log.error(e.getCause, "Exception in MetricsServiceHandler")
  }
}