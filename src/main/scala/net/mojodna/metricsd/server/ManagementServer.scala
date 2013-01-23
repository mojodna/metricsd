package net.mojodna.metricsd.server

import java.util.concurrent.Executors
import net.mojodna.metricsd.server.ManagementServiceHandler
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.util.CharsetUtil
import org.jboss.netty.handler.codec.string.{StringDecoder, StringEncoder}
import org.jboss.netty.channel.{Channels, ChannelPipeline, ChannelPipelineFactory}
import org.jboss.netty.handler.codec.frame.{Delimiters, DelimiterBasedFrameDecoder}
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import com.codahale.logula.Logging
import java.net.InetSocketAddress

class ManagementServer(port: Int) extends Logging {
  def listen = {

    val b = new ServerBootstrap(
      new NioServerSocketChannelFactory(
        Executors.newCachedThreadPool,
        Executors.newCachedThreadPool
      )
    )

    b.setPipelineFactory(new ChannelPipelineFactory {
      def getPipeline: ChannelPipeline = {
        val pipeline = Channels.pipeline

        pipeline.addLast(
          "framer",
          new DelimiterBasedFrameDecoder(
            512, //Geez how big a command do we expect
            Delimiters.lineDelimiter:_*
          )
        )
        pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8))
        pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8))
        pipeline.addLast("handler",  new ManagementServiceHandler)

        pipeline
      }
    })

    log.info("Listening on port %d.", port)
    b.bind(new InetSocketAddress(port))
  }
}

object ManagementServer {
  val DEFAULT_PORT = 8126
}
