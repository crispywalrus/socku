package net.crispywalrus
package socku
package handlers

import akka.actor._
import akka.stream._
import akka.stream.actor.ActorPublisher
import org.mashupbots.socko.events._
import org.mashupbots.socko.routes._

object HttpFlowHandler {
  def props: Props = Props[HttpFlowHandler]
}

class HttpFlowHandler extends ActorPublisher[HttpRequestEvent] {
  var count = 0
  def receive = {
    case evt: HttpRequestEvent => 
      count += 1
      evt.response.write(
        """["GET %s", "%s"]""".format(evt.request.endPoint.path, count),
        "application/json"
      )
  }
}

