package net.crispywalrus.socku

import org.mashupbots.socko.events._
import org.mashupbots.socko.routes._
import org.mashupbots.socko.webserver.{ WebServer, WebServerConfig }
import akka.actor.{ Actor, ActorSystem, Props }

class Hello extends Actor {
  def receive = {
    case event: HttpRequestEvent =>
      event.response.write("Hello ")
      context.stop(self)
  }
}

object SockuApp extends App {

  val system = ActorSystem("SockuWebapp")

  val routes = Routes({
    case GET(request) => {
      system.actorOf(Props[Hello]) ! request
    }
  })

  val webServer = new WebServer(WebServerConfig(port=8080), routes, system)
  webServer.start()

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run { webServer.stop() }
  })

}
