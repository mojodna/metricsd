package net.mojodna.metricsd.server

import _root_.metricsd.server.{MetricsServiceHandler}
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ConnectionlessBootstrap
import org.jboss.netty.util.CharsetUtil
import org.jboss.netty.handler.codec.string.{StringDecoder, StringEncoder}
import org.jboss.netty.channel.{FixedReceiveBufferSizePredictorFactory, Channels, ChannelPipeline, ChannelPipelineFactory}
import com.codahale.logula.Logging
import java.net.InetSocketAddress

class MetricsServer(port: Int) extends Logging {
  def listen = {
    val f = new NioDatagramChannelFactory(Executors.newCachedThreadPool)

    val b = new ConnectionlessBootstrap(f)

    // Configure the pipeline factory.
    b.setPipelineFactory(new ChannelPipelineFactory {
      def getPipeline: ChannelPipeline = {
        Channels.pipeline(
          new StringEncoder(CharsetUtil.UTF_8),
          new StringDecoder(CharsetUtil.UTF_8),
          new MetricsServiceHandler
        )
      }
    })

    b.setOption("broadcast", "false")

    b.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(1024))

    log.info("Listening on port %d.", port)
    b.bind(new InetSocketAddress(port))
  }
}

object MetricsServer {
  val DEFAULT_PORT = 8125
}