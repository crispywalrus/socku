package net.crispywalrus
package socku
package coordination

import akka.actor._
import concurrent.duration._

case class Token(count: Int=1) extends AnyVal
case class Consume(count: Int=1,delay: Duration=Duration.Inf)
case class Consumed(success: Boolean) extends AnyVal

class TokenBucket(maxTokens: Int,initialTokens: Int=0) extends Actor {
  private [this] var currentTokens = initialTokens
  def receive = {
    case Token(t) => currentTokens = maxOrResult(currentTokens+t)
    case Consume(t,w) => if( t > currentTokens ) {
      val required = t - currentTokens
      currentTokens = 0
      waitForMore(sender,required)
    } else {
      currentTokens = currentTokens - t
      sender ! Consumed(true)
    }
  }

  def waitForMore(sender: ActorRef, required: Int): Unit = {
  }

  def maxOrResult(count: Int): Int = if( count > maxTokens ) maxTokens else count
}


