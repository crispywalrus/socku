package net.crispywalrus
package socku
package model

import java.util.Date //FIXME!
import simulacrum._
import machinist.DefaultOps
import scala.language.implicitConversions
import scala.reflect.ClassTag

trait Key {
  def key: String
}

trait Entity[E,M] {
  def get(k: Key): E
  def put(k: Key,e: E): M
  def delete(k: Key): M
}

case class S(name: String,description: String,ts: List[T])
case class T(created: Date,updated: Date,name: String,us: List[U])
case class U(created: Date,createdBy: S,name: String,body: String)

