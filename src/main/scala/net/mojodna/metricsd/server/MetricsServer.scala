package net.mojodna.metricsd.server

import java.util.concurrent.Executors
import org.jboss.netty.util.CharsetUtil
import org.jboss.netty.handler.codec.string.{StringDecoder, StringEncoder}
import org.jboss.netty.channel.{FixedReceiveBufferSizePredictorFactory, Channels, ChannelPipeline, ChannelPipelineFactory}
import com.codahale.logula.Logging
import java.net.InetSocketAddress
import org.jboss.netty.bootstrap.{ServerBootstrap, ConnectionlessBootstrap}
import org.jboss.netty.channel.socket.nio.{NioServerSocketChannelFactory, NioDatagramChannelFactory}

class MetricsServer(port: Int) extends Logging {
  def listen = {
    val b = new ServerBootstrap(
      new NioServerSocketChannelFactory(
        Executors.newCachedThreadPool,
        Executors.newCachedThreadPool
      )
    )

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

    log.info("Listening on port %d.", port)
    b.bind(new InetSocketAddress(port))
  }
}

object MetricsServer {
  val DEFAULT_PORT = 8125
}