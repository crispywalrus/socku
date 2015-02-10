package net.crispywalrus
package socku
package model

import simulacrum._
import machinist.DefaultOps
import scala.concurrent.Future
import scala.language.implicitConversions

trait Key {
  def key: String
}

trait VersionedKey extends Key {
  def ref: String
}

trait KvStore[T,M] {
  def get(key: Key): Future[Option[T]]
  def get(key: VersionedKey): Future[Option[T]]
  def list(limit: Int): Future[List[T]]
  def put(key: Key,value: T): Future[M]
  def put(key: VersionedKey,value: T): Future[M]
  def insert(key: Key,value: T): Future[M]
  def patch(key: Key,adds: Map[String,String],moves: Map[String,String],tests: Map[String,String]): Future[M]
  def patch(key: VersionedKey,adds: Map[String,String],moves: Map[String,String],tests: Map[String,String]): Future[M]
  def delete(key: Key,purge: Boolean): Future[Boolean]
}

trait GraphStore[T] {
  def get(key: Key,relation: Symbol): Future[AnyRef]
  def put(source: Key,related: Key,relation: Symbol): Future[Boolean]
}
