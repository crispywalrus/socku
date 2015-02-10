package net.crispywalrus
package socku
package handlers

import akka.actor._
import akka.stream._
import akka.stream.actor.ActorPublisher
import org.mashupbots.socko.events._
import org.mashupbots.socko.routes._

class GetUser extends Actor {
  def receive = {
    case HttpRequest(request) => request match {
      case GET(Path("/user")) => {
        // get user defined in request or not
        request.response.write(HttpResponseStatus.NOT_FOUND)
      }
    }
  }
}

