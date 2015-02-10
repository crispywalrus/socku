package net.crispywalrus
package socku

import scala.reflect.ClassTag

package object model {
  def classy[T : ClassTag] = implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]
}
