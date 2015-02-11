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

trait KvStore[T] {
  def get(key: Key): Future[Option[T]]
  def get(key: VersionedKey): Future[Option[T]]
  def list(limit: Int): Future[List[T]]
  def put(value: T): Future[VersionedKey]
  def put(key: Key,value: T): Future[VersionedKey]
  def put(key: VersionedKey,value: T): Future[VersionedKey]
  def insert(key: Key,value: T): Future[VersionedKey]
  def patch(key: Key,adds: Map[String,String],moves: Map[String,String],tests: Map[String,String]): Future[VersionedKey]
  def patch(key: VersionedKey,adds: Map[String,String],moves: Map[String,String],tests: Map[String,String]): Future[VersionedKey]
  def delete(key: Key): Future[Boolean]
}

trait GraphStore[T,Q] {
  def get(key: Key,relation: Symbol): Future[Q]
  def put(source: Key,related: Key,relation: Symbol): Future[Boolean]
  def delete(origin: Key,related: Key)
}
