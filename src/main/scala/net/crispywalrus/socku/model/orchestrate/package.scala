package net.crispywalrus
package socku
package model

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.config.{ Config, ConfigFactory }
import io.orchestrate.client._
import net.crispywalrus.socku.SockuApp
import concurrent._
import reflect.ClassTag
import scala.language.implicitConversions

package object orchestrate {

  type id[K, T] = (K) => T

  val config = ConfigFactory.load().getConfig("socku-orchestrate")

  def mkClient(mapper: ObjectMapper,config: Config = config) = OrchestrateClient.builder(config.getString("apiKey"))
    .mapper(mapper)
    .poolSize(config.getInt("poolSize"))
    .maxPoolSize(config.getInt("maxPoolSize"))
    .build()

  implicit def adapter[T](s: (T) => Unit, f: (Throwable) => Unit): ResponseAdapter[T] =
    new ResponseAdapter[T]() {
      override def onFailure(error: Throwable) = f(error)
      override def onSuccess(obj: T) = s(obj)
    }

}

