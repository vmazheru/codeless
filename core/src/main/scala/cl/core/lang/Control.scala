package cl.core.lang

import scala.language.reflectiveCalls

object Control {

  /**
   * Automatic closable resource management
   * (from the book, Beginning Scala, by David Pollak)
   */
  def using[A <: { def close(): Unit }, B](resource: A)(f: A => B): B =
    try {
      f(resource)
    } finally {
      resource.close()
    }
    
}