package net.crispywalrus
package socku
package model
package orchestrate

import io.orchestrate.client._
import io.orchestrate.client.jsonpatch._
import collection.JavaConversions._
import concurrent._
import reflect.ClassTag
import scala.util.{ Try, Success, Failure }
import scala.language.implicitConversions

case class KvKey(key: String) extends Key
case class KvRefKey(key: String,ref: String) extends VersionedKey

/**
 * collections are groupings of a single type of object so we model
 * it as a typed collection with a custom api.
 */
class Collection[T : ClassTag](client: OrchestrateClient,collection: String) extends KvStore[T,KvMetadata] {

  type id[K, T] = (K) => T

  def search: CollectionSearchResource = client.searchCollection(collection)
  def events(key: Key): EventResource = client.event(collection,key.key)
  def relation(key: Key): RelationResource = client.relation(collection,key.key)
  def resource(key: Key): KvResource = client.kv(collection, key.key)
  def resource(key: VersionedKey): KvResource = client.kv(collection, key.key)
  def resource: KvListResource = client.listCollection(collection)

  def cb[K, T](s: id[K, T], p: Promise[T]): ResponseAdapter[K] = {
    new ResponseAdapter[K]() {
      override def onSuccess(obj: K) = p.success(s(obj))
      override def onFailure(ex: Throwable) = p.failure(ex)
    }
  }

  /**
    * many methods return metadata, this does the identity transform for them
    */
  def metafunc: id[KvMetadata, KvMetadata] = (kvm: KvMetadata) => kvm

  /**
    * get the latest value associated with the given key
    */
  def get(key: Key): Future[Option[T]] = {
    def getf[Z]: id[KvObject[Z], Option[Z]] = (o: KvObject[Z]) => if (o != null) Option(o.getValue) else None
    val p = Promise[Option[T]]()
    resource(key).get(classy[T]).on(cb[KvObject[T], Option[T]](getf, p))
    p.future
  }

  /**
    * get a specific version of a key, this could actually be a value
    * from back in history and not the current value.
    */
  def get(key: VersionedKey): Future[Option[T]] = {
    def getf: id[KvObject[T],Option[T]] = (o: KvObject[T]) => if (o != null) Option(o.getValue) else None
    val p = Promise[Option[T]]()
    resource(key).get(classy[T], key.ref).on(cb[KvObject[T], Option[T]](getf, p))
    p.future
  }

  /**
    * get the latest value associated with the given key
    */
  def list(lim: Int=20): Future[List[T]] = {
    def listf: id[KvList[T],List[T]] = (lr: KvList[T]) => lr.iterator().map( kvo => kvo.getValue ).toList
    val p = Promise[List[T]]()
    resource.limit(lim).get(classy[T]).on(cb[KvList[T],List[T]](listf, p))
    p.future
  }

  def delete(key: Key, purge: Boolean = false): Future[Boolean] = {
    def deletef: id[java.lang.Boolean, Boolean] = (o: java.lang.Boolean) => o
    val p = Promise[Boolean]()
    resource(key).delete(purge)
      .on(cb(deletef, p))
    p.future
  }

  def post(obj: T): Future[KvMetadata] = {
    val p = Promise[KvMetadata]()
    client.postValue(collection,obj).on(cb(metafunc, p))
    p.future
  }

  def put(key: Key, obj: T): Future[KvMetadata] = {
    val p = Promise[KvMetadata]()
    resource(key).put(obj).on(cb(metafunc, p))
    p.future
  }

  def insert(key: Key, obj: T): Future[KvMetadata] = {
    val p = Promise[KvMetadata]()
    resource(key).ifAbsent.put(obj).on(cb(metafunc, p))
    p.future
  }

  def put(key: VersionedKey, obj: T): Future[KvMetadata] = {
    def putD(key: VersionedKey, obj: T)(implicit tag: ClassTag[T]): Future[KvMetadata] = {
    val p = Promise[KvMetadata]()
    resource(key).ifMatch(key.ref).put(obj).on(cb(metafunc, p))
    p.future
    }
    putD(key,obj)
  }

  def patch(key: Key, adds: Map[String, String], moves: Map[String, String], tests: Map[String, String]): Future[KvMetadata] = {
    val p = Promise[KvMetadata]()
    val patch = JsonPatch.builder()
    tests map { case (key, value) => patch.test(key, value) }
    adds.map { case (key, value) => patch.add(key, value) }
    moves.map { case (key, value) => patch.add(key, value) }
    resource(key).patch(patch.build).on(cb(metafunc, p))
    p.future
  }

  def patch(key: VersionedKey, adds: Map[String, String] = Map(), moves: Map[String, String] = Map(), tests: Map[String, String] = Map()): Future[KvMetadata] = {
    val p = Promise[KvMetadata]()
    val patch = JsonPatch.builder()
    tests map { case (key, value) => patch.test(key, value) }
    adds.map { case (key, value) => patch.add(key, value) }
    moves.map { case (key, value) => patch.add(key, value) }
    resource(key).patch(patch.build).on(cb(metafunc, p))
    p.future
  }

  def merge(key: Key, jsonMerge: String): Future[KvMetadata] = {
    val p = Promise[KvMetadata]()
    resource(key).merge(jsonMerge).on(cb(metafunc, p))
    p.future
  }

}
