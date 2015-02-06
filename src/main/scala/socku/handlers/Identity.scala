package net.crispywalrus
package socku
package service
package user

import util.{ Try, Success, Failure }
import akka.actor._
import reflect.ClassTag
import io.orchestrate.client._

case class User()
case class Credentials(userName: String,passphrase: String)

sealed trait UserMessage
case class Authenticate(credentials: Credentials) extends UserMessage
case class Register(credentials: Credentials) extends UserMessage

class Identity extends Actor {
  def receive = {
    case Authenticate(creds) => {
      // log user in or fail
    }
  }
}

case class Key(collection: String,key: String)

trait IdentityDriver {
  def root: String
  def client: Client
  def fetch[T : ClassTag](key: Key)(implicit tag: ClassTag[T]): Option[T] = {
    val p = scala.concurrent.Promise
    Option(client.kv(key.collection,key.key).get(tag.runtimeClass.asInstanceOf[Class[T]]).get().getValue())
  }
}
