package net.crispywalrus
package socku
package model
package redis

import concurrent.Future

import brando._

class Redis[T, M] extends KeyStore[T] {
  def delete(key: Key): Future[Boolean] = null
  def get(key: Key): Future[Option[T]] = null
  def get(key: VersionedKey): Future[Option[T]] = null
  def insert(key: Key, value: T): Future[VersionedKey] = null
  def list(limit: Int): Future[List[T]] = throw new UnsupportedOperationException("")
  def patch(key: Key, adds: Map[String, String], moves: Map[String, String], tests: Map[String, String]): Future[VersionedKey] =
    throw new UnsupportedOperationException("")
  def patch(key: VersionedKey, adds: Map[String, String], moves: Map[String, String], tests: Map[String, String]): Future[VersionedKey] =
    throw new UnsupportedOperationException("")
  def put(value: T): Future[VersionedKey] = null
  def put(key: Key,value: T): Future[VersionedKey] = null
  def put(key: VersionedKey,value: T): Future[VersionedKey] = null
}
