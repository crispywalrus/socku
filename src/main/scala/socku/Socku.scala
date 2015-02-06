package net.crispywalrus
package socku

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

case class Something(event: HttpRequestEvent)

class GetStream extends ActorPublisher[HttpRequestEvent] {
  var count = 0
  def receive = {
    case msg: Something => {
      count += 1
      msg.event.response.write(
        """{"GET %s", "%s"}""".format(msg.event.request.endPoint.path, count),
        "application/json"
      )
    }
  }
}

/**
 * this is rapidly becoming a rubbish tip for any and every piece of global-ish trash
 */
object SockuApp extends App {

  implicit val system = ActorSystem("SockuWebapp")
  implicit val materializer = FlowMaterializer()

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.registerModule(new AfterburnerModule())

  object SockuWebConfig extends ExtensionId[WebServerConfig] with ExtensionIdProvider {
    override def lookup = SockuWebConfig
    override def createExtension(system: ExtendedActorSystem) =
      new WebServerConfig(system.settings.config, "socku-http")
  }
  val sockuWebConfig = SockuWebConfig(system)

  val routes = Routes({
    case HttpRequest(request) => request match {
      case GET(Path("/favicon.ico")) => request.response.write(HttpResponseStatus.NOT_FOUND)
      case GET(_) => stream ! Something(request)
    }
  })

  val stream = system.actorOf(Props[GetStream], "stream")

  val webServer = new WebServer(sockuWebConfig, routes, system)
  webServer.start()

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run { webServer.stop() }
  })

}
