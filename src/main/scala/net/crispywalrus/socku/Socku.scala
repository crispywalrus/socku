package net.crispywalrus
package socku

import com.softwaremill.macwire.Macwire
import org.mashupbots.socko.events._
import org.mashupbots.socko.routes._
import org.mashupbots.socko.webserver.{ WebServer, WebServerConfig }
import akka.actor._
import akka.stream._
import akka.stream.actor.ActorPublisher
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.afterburner.AfterburnerModule
import io.orchestrate.client._
import net.crispywalrus.socku.handlers._
import com.softwaremill.macwire.MacwireMacros._


trait SystemModule extends Macwire {
  implicit val system = ActorSystem("socku-webapp")
  lazy val materializer = ActorFlowMaterializer()

  lazy val mapper = {
    val om = new ObjectMapper()
    om.registerModule(DefaultScalaModule)
    om.registerModule(new AfterburnerModule())
    om
  }
}

trait WebModule extends Macwire with SystemModule {

  object SockuWebConfig extends ExtensionId[WebServerConfig] with ExtensionIdProvider {
    override def lookup = SockuWebConfig
    override def createExtension(system: ExtendedActorSystem) =
      new WebServerConfig(system.settings.config, "socku-http")
  }

  lazy val sockuWebConfig = SockuWebConfig(system)

  lazy val routes = Routes({
    case HttpRequest(request) => request match {
      case GET(Path("/favicon.ico")) => request.response.write(HttpResponseStatus.NOT_FOUND)
      case GET(_) => http ! request
    }
    case WebSocketFrame(frame) => ws ! frame
    case WebSocketHandshake(handshake) => ws ! handshake
  })

  val http = system.actorOf(HttpFlowHandler.props, "http-flow")
  val ws = system.actorOf(WsFlowHandler.props, "ws-flow")
}

/**
 * this is rapidly becoming a rubbish tip for any and every piece of global-ish trash
 */
object SockuApp extends App with WebModule {

  val webServer = new WebServer(sockuWebConfig, routes, system)
  webServer.start()

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run { webServer.stop() }
  })

}
