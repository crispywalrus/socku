package net.crispywalrus
package socku

import linx._
import org.mashupbots.socko.events._
import org.mashupbots.socko.routes._

package object entities {
}

package entities {

  trait SockuEntity[A] {
    def name: String
    def writeResponse(evt: SockoEvent,data: A): Unit
    def routes: () => Seq[PartialFunction[SockoEvent, Unit]]
    val root = Root / "entity"
    def get: PartialFunction[SockoEvent, Unit] = {
      case req @ GET(Path(name)) => {} // get all, probably with pagination
      case req @ GET(PathSegments(name::id::Nil)) => {} // get an item, if there's a continuing path then drill in
      case req @ PUT(Path(name)) => {} // create
      case req @ POST(Path(name)) => {} // create
      case req @ POST(PathSegments(name :: id :: Nil)) => {} // update
      case req @ DELETE(PathSegments(name :: id :: Nil)) => {} // delete
    }
  }

}

