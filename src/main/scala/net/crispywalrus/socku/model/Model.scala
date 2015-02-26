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

/**
  * KvStore represents a collection of keyed items all of the same type. 
  */
trait KeyStore[T] {
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

trait KeyedCollection[T] {
  trait Collected {
    def collection: KeyedCollection[T]
  }

  case class KvKey(key: String,collection: KeyedCollection[T]) extends Key with Collected
  case class KvRefKey(key: String, ref: String,collection: KeyedCollection[T]) extends VersionedKey with Collected

  def collection: String
  def apply(key: String): Key = KvKey(key,this)
  def apply(key: String,ref: String): VersionedKey = KvRefKey(key,ref,this)
}

case class Relation[T](kind: Symbol,collection: KeyedCollection[T])
case class PaginatedList[T](items: List[T],hasNext: Boolean)

/**
 * GraphStore represents the set of relationships between one kind and
 * another. The type limit is totally an artifact of the underlying
 * driver but still affects usage.
 */
trait GraphStore {
  def get(key: Key,relation: Relation[VersionedKey]): Future[List[VersionedKey]]
  def put(source: Key,relation: Relation[VersionedKey],related: Key): Future[Boolean]
  def delete(origin: Key,related: Key,relation: Relation[VersionedKey])
}
