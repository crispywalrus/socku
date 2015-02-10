package net.crispywalrus
package socku
package coordination

import com.typesafe.config.ConfigFactory
import org.scalatest._, Matchers._
import akka.actor._
import akka.pattern._
import akka.testkit.{ TestActors, DefaultTimeout, ImplicitSender, TestActorRef, TestKit }
import scala.concurrent._
import scala.concurrent.duration._

class TimestampSpec extends TestKit(
  ActorSystem(
    "TimestampSpec",
    ConfigFactory.parseString(TimestampSpecConfig.config)
  )
)
  with FlatSpecLike with BeforeAndAfterAll with DefaultTimeout {

  val f = 1593626063592751104L

  override def afterAll {
    shutdown()
  }

  import system.dispatcher

  val longstamp = system.actorOf(Props(new LongClockTimestamp(0, 0)))
  val bigintstamp = system.actorOf(Props(new BigIntClockTimestamp(0)))
  val stringstamp = system.actorOf(Props(new StringClockTimestamp(0)))

  "LongTimestamp" should "generate an id per call" in {
    val z = ask(longstamp, "next") map { x =>
      // x has no defined value other than be greater than our timestamp generators epoch
      x.asInstanceOf[Long] should be > f
    }
    Await.result(z, 1 seconds)
  }
  it should "generate a sequence of unique ids" in {
    val ids = scala.collection.mutable.Set[Long]()
    (1L to 1000L).foreach({ i ⇒
      {
        // we don't really care about the asynchronisity, so just wait
        val f = ask(longstamp, "next")
        ids += Await.result(f.asInstanceOf[Future[Long]], 2 second)
      }
    })
    ids.size should be(1000)
  }

  "StringTimestamp" should "generate unique ids" in {
    var rounds = 0
    val ids = scala.collection.mutable.Set[String]()
    (1L to 1000L).foreach(i ⇒ {
      rounds = rounds + 1
      val f = ask(stringstamp, "next")
      ids += Await.result(f.asInstanceOf[Future[String]], 10 second)
    })
    println("rounds " + rounds)
    ids.size should be(1000)
  }

  "BigIntTimestamp" should "generate a sequence of unique ids" in {
    val ids = scala.collection.mutable.Set[BigInt]()
    (1L to 1000L).foreach(i ⇒ {
      val f = ask(bigintstamp, "next")
      ids += Await.result(f.asInstanceOf[Future[BigInt]], 2 second)
    })
    ids.size should be(1000)
  }
}

object TimestampSpecConfig {
  def config: String = ""
}
