package net.crispywalrus
package socku
package model
package orchestrate

import io.orchestrate.client._
import io.orchestrate.client.jsonpatch._
import com.fasterxml.jackson.databind.ObjectMapper
import collection.JavaConversions._
import concurrent._
import reflect.ClassTag
import scala.util.{ Try, Success, Failure }
import scala.language.implicitConversions
import com.netaporter.uri.Uri
import com.netaporter.uri.dsl._

case class KeyValueEntity[T : ClassTag](uri: Uri)(implicit mapper: ObjectMapper) {
  def serialize(t: T): String = mapper.writeValueAsString(t)
  def deserialize(s: String): T = mapper.readValue(s,classy[T])
}

trait Driver {
  implicit def mapper: ObjectMapper
  val baseUri = "https://api.orchestrate.io/v0"
  def kv[T : ClassTag](collection: String,key: Key): KeyValueEntity[T] = KeyValueEntity[T](baseUri / collection / key.key)
  def kv[T : ClassTag](collection: String,key: VersionedKey): KeyValueEntity[T] = KeyValueEntity[T](baseUri / collection / key.key / "ref" / key.ref)
}

abstract class Graph(client: OrchestrateClient) extends GraphStore {
  def relation(key: Key,collection: String): RelationResource = client.relation(collection, key.key)
  def extractKeys[T] : id[RelationList[T],List[VersionedKey]] = rl => List()
  def extractValues[T] : id[RelationList[T],List[T]] = rl => List()

  def mkFuture[I, O](req: OrchestrateRequest[I], x: id[I, O]): Future[O] = {
    def cb[K, T](s: id[K, T], p: Promise[T]): ResponseAdapter[K] = new ResponseAdapter[K]() {
      override def onSuccess(obj: K) = p.success(s(obj))
      override def onFailure(ex: Throwable) = p.failure(ex)
    }
    val p = Promise[O]()
    req.on(cb[I, O](x, p))
    p.future
  }

  def get[T : ClassTag](key: Key,relationKey: Relation[VersionedKey]): Future[List[VersionedKey]] = {
    mkFuture(relation(key,relationKey.collection.collection).get(classy[T],relationKey.kind.toString),extractKeys[T])
  }
}

/**
 * collections are groupings of a single type of object so we model
 * it as a typed collection with a custom api.
 */
class Collection[T: ClassTag](client: OrchestrateClient,val collection: String) extends KeyedCollection[T] with KeyStore[T] {

  def search: CollectionSearchResource = client.searchCollection(collection)
  def events(key: Key): EventResource = client.event(collection, key.key)
  def resource(key: Key): KvResource = client.kv(collection, key.key)
  def resource(key: VersionedKey): KvResource = client.kv(collection, key.key)
  def resource: KvListResource = client.listCollection(collection)

  /**
   * many methods return metadata, this does the identity transform
   * for them. In order for the compiler to understand the types this
   * function needs to have a type attached, so there's no advantage
   * to an anonymous closure.
   */
  val metafunc: id[KvMetadata, KvMetadata] = { identity(_) }
  val keyfunc: id[KvMetadata, VersionedKey] = (m: KvMetadata) => KvRefKey(m.getKey, m.getRef, this)
  val getf: id[KvObject[T], Option[T]] = (o: KvObject[T]) => if (o != null) Option(o.getValue) else None
  val listf: id[KvList[T], List[T]] = (lr: KvList[T]) => lr.iterator().map(kvo => kvo.getValue).toList
  val deletef: id[java.lang.Boolean, Boolean] = (o: java.lang.Boolean) => o

  def mkFuture[I, O](req: OrchestrateRequest[I], x: id[I, O]): Future[O] = {
    def cb[K, T](s: id[K, T], p: Promise[T]): ResponseAdapter[K] = new ResponseAdapter[K]() {
      override def onSuccess(obj: K) = p.success(s(obj))
      override def onFailure(ex: Throwable) = p.failure(ex)
    }

    val p = Promise[O]()
    req.on(cb[I, O](x, p))
    p.future
  }

  /**
   * get the latest value associated with the given key
   */
  def get(key: Key): Future[Option[T]] = mkFuture(resource(key).get(classy[T]), getf)

  /**
   * get a specific version of a key, this could actually be a value
   * from back in history and not the current value.
   */
  def get(key: VersionedKey): Future[Option[T]] = mkFuture(resource(key).get(classy[T]), getf)

  /**
   * a list of all items in a collection
   */
  def list(lim: Int = 20): Future[List[T]] = mkFuture(resource.limit(lim).get(classy[T]), listf)

  /**
   * a list of all items in a collection starting at startKey.
   */
  def list(lim: Int, startKey: Key, inclusive: Boolean): Future[List[T]] = mkFuture(
    resource.startKey(startKey.key).inclusive(inclusive).limit(lim).get(classy[T]),
    listf
  )

  /**
   * unconditionally delete the value at key. if purge is true then
   * the key and ref history is deleted.
   */
  def delete(key: Key): Future[Boolean] = mkFuture(resource(key).delete(true), deletef)

  /**
   * upsert a value into the store and autogenerate a key.
   */
  def put(obj: T): Future[VersionedKey] = mkFuture(client.postValue(collection, obj), keyfunc)

  /**
   * upsert a value into the store at key.
   */
  def put(key: Key, obj: T): Future[VersionedKey] = mkFuture(resource(key).put(obj), keyfunc)

  /**
   * put an object into the store if, and only if, the current version
   * (the ref) matches the supplied reference. The returned value will
   * be either the new ref or a failure indicator.
   */
  def put(key: VersionedKey, obj: T): Future[VersionedKey] = mkFuture(resource(key).ifMatch(key.ref).put(obj), keyfunc)

  /**
   *  put and object into the store only if there is no value
   *  associated with the key. a normal put simply does and upsert
   *  this adds the ability to fail if the key has already been used.
   */
  def insert(key: Key, obj: T): Future[VersionedKey] = insert(key, obj, true)

  /**
   *  put and object into the store only if there is no value
   *  associated with the key. a normal put simply does and upsert
   *  this adds the ability to fail if the key has already been used.
   */
  def insert(key: Key, obj: T, ifAbsent: Boolean): Future[VersionedKey] = mkFuture(resource(key).ifAbsent(ifAbsent).put(obj), keyfunc)

  /**
   * a patch call has three possible types of field effect and these
   * are passed in via a JsonPatch object. This constructs and builds
   * that object.
   */
  def patcher(adds: Map[String, String], moves: Map[String, String], tests: Map[String, String]) = {
    val patch = JsonPatch.builder()
    tests map { case (key, value) => patch.test(key, value) }
    adds.map { case (key, value) => patch.add(key, value) }
    moves.map { case (key, value) => patch.add(key, value) }
    patch.build
  }

  /**
   * changing a value with put requires the object to have been read
   * first. this allows for changes to an object without that initial
   * read.
   */
  def patch(key: Key, adds: Map[String, String], moves: Map[String, String], tests: Map[String, String]): Future[VersionedKey] =
    mkFuture(resource(key).patch(patcher(adds, moves, tests)), keyfunc)

  /**
   * a slightly more constricted patch, adding a ref... although
   * without a read it's questionable if you can actually do
   * this. changing a value with put requires the object to have been
   * read first. this allows for changes to an object without that
   * initial read.
   */
  def patch(
    key: VersionedKey,
    adds: Map[String, String] = Map(),
    moves: Map[String, String] = Map(),
    tests: Map[String, String] = Map()
  ): Future[VersionedKey] = mkFuture(resource(key).patch(patcher(adds, moves, tests)), keyfunc)

  def merge(key: Key, jsonMerge: String): Future[VersionedKey] = mkFuture(resource(key).merge(jsonMerge), keyfunc)

}

