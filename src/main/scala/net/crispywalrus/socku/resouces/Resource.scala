package net.crispywalrus
package socku
package entity

/** placeholder */
trait DbAdapter[E]

/**
  * simplest resource representation. R is the class of the objects
  * that are the materialization of the data. Path is the http resouce
  * path. db is the db adapter that persists and retrieves instances
  * of the resouces entities. The idea is that a db/store/adapter
  * reads and writes only one type of thing (i.e. one path only
  * returns a single kind of thing.) There is a fourth possible
  * dimenssion to this, the subset of the local object model that get
  * serialized back to the client... maybe.
  */
trait Resource[E] {
  def path: String
  def dba: DbAdapter[E]

  
}

class HttpResource[E](val path: String,val dba: DbAdapter[E]) extends Resource[E] {
}
