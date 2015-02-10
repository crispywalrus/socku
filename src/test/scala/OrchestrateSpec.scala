package net.crispywalrus
package socku
package model
package orchestrate

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.config.ConfigFactory
import io.orchestrate.client._
import org.scalatest._, Matchers._
import collection.JavaConversions._
import scala.concurrent._
import scala.concurrent.duration._
import java.util.Date

case class S(name: String,description: String,ts: List[T])
case class T(created: Date,updated: Date,name: String,us: List[U])
case class U(created: Date,createdBy: S,name: String,body: String)

class OrchestrateSpec extends FlatSpec with Matchers {

  import com.typesafe.config.ConfigFactory
  val oConf = ConfigFactory.load().getConfig("socku-orchestrate")

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  val client = mkClient(mapper)
  val c = new Collection[S](client,"users")

  "Store client" should "ping orchestrate" in {
    client.ping
  }
  it should "list a collection" in {
    val list = client.listCollection("users").get(classOf[S]).get()
    list.map(u => u.getValue.name)
  }

  val bobKey = KvKey("ralph")
  val ralphKey = KvKey("ralph")
  val bob = new S("bob","sometext",List())
  val ralph = new S("ralph","ralphs text",List())

  "Collection" should "support put kv operation" in {
    Await.result(c.put(bobKey, bob),1 second)
  }
  it should "support get kv operation" in {
    Await.result(c.put(bobKey, bob),1 second)
    val u = Await.result(c.get(bobKey),1 second)
    u.get.name should === ("bob")
  }
  it should "support getref kv operation" in {
    val md = Await.result(c.put(ralphKey,ralph),4 second)
    val ralphRefKey = new KvRefKey(ralphKey.key,md.getRef())
    val u = Await.result(c.get(ralphRefKey),4 second)
    u.map(usr => usr.name should === ("ralph")).getOrElse(false should === (true))
  }
  it should "support delete kv operation" in {
    Await.result(c.delete(KvKey("bob"),true),1 second) should === (true)
  }
}
