package net.crispywalrus.socku

import org.mashupbots.socko.events._
import org.mashupbots.socko.routes._
import org.mashupbots.socko.webserver.{ WebServer, WebServerConfig }
import akka.actor.{ Actor, ActorSystem, Props }
import akka.stream._
import akka.stream.actor.ActorPublisher

case class Something(event: HttpRequestEvent)

class GetStream extends ActorPublisher[HttpRequestEvent] {
  var count = 0
  def receive = {
    case msg: Something => {
      count += 1
      msg.event.response.write("GET %s, %s"format(msg.event.request.endPoint.path,count))
    }
  }
}

object SockuApp extends App {

  implicit val system = ActorSystem("SockuWebapp")
  implicit val materializer = FlowMaterializer()

  val stream = system.actorOf(Props[GetStream], "stream")

  val routes = Routes({
    case HttpRequest(request) => request match {
      case GET(Path("/favicon.ico")) => request.response.write(HttpResponseStatus.NOT_FOUND)
      case GET(_) => stream ! Something(request)
    }
  })

  val webServer = new WebServer(WebServerConfig(), routes, system)
  webServer.start()

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run { webServer.stop() }
  })

}
