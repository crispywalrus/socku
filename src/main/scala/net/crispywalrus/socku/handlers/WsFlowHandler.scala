package net.crispywalrus
package socku
package handlers

import akka.actor._
import akka.stream._
import akka.stream.actor.ActorPublisher
import org.mashupbots.socko.events._
import org.mashupbots.socko.routes._

object WsFlowHandler {
  def props: Props = Props[WsFlowHandler]
}

class WsFlowHandler extends ActorPublisher[WebSocketFrameEvent] {
  var count = 0
  def receive = {
    case event: WebSocketHandshakeEvent => {
    }
    case event: WebSocketFrameEvent => {
    }
  }
}

