package net.crispywalrus
package socku
package coordination

import akka.actor.{ Actor, ActorLogging }
import akka.event.Logging

trait Generator[T] {
  val epoch = 1041654066000L // start epoch at January 3,2003 21:21 PST
  var lastTimestamp = -1L
  var sequence: Long
  def sequenceMask: Long
  def timestampShift: Long
  def clusterId: Long
  def clusterIdShift: Long
  def workerId: Long
  def workerIdShift: Long

  def finish(timestamp: Long, timestampShift: Long, clusterId: Long, clusterIdShift: Long, workerId: Long, workerIdShift: Long, sequence: Long): T

  def next: T = {
    var timestamp = timeGen()

    if (timestamp < lastTimestamp) {
      // current millis is less than previous millis,bitch about it
      // log.error("clock is moving backwards.  Rejecting requests until %d.".format(lastTimestamp));
      throw new RuntimeException("Clock moved backwards.  Refusing to generate id for %d milliseconds".format(lastTimestamp - timestamp))
    } else if (lastTimestamp == timestamp) {
      // yes,increment the counter
      sequence = (sequence + 1) & sequenceMask
      if (sequence == 0) {
        // unless we get a roll over,the wait till next milli
        timestamp = tilNextMillis(lastTimestamp)
      }
    } else {
      // else new milli,reset sequence counter
      sequence = 0
    }

    // for next timestamp
    lastTimestamp = timestamp

    // nasty bit match to make a 64 bit number out of all our components
    finish(timestamp, timestampShift, clusterId, clusterIdShift, workerId, workerIdShift, sequence)
  }

  def tilNextMillis(lastTimestamp: Long): Long = {
    var timestamp = timeGen()
    while (timestamp <= lastTimestamp) {
      timestamp = timeGen()
    }
    timestamp
  }

  def timeGen(): Long = System.currentTimeMillis()
}

/**
 * generate a unique Long id. each id is a lamport timestamp,42
 * bits of time,8 bits of worker id,2 bits of cluster id and a 12 bit
 * sequence counter. This gives this class an effective lifespan of 140 years
 * and allows for 8k timestamps per millisecond.
 *
 */
class LongTimestamp(
  val workerId: Long,
  val clusterId: Long,
  workerIdBits: Long = 8L,
  clusterIdBits: Long = 2L,
  var sequence: Long = 0L,
  val sequenceBits: Long = 12L
) extends Actor with Generator[Long] {
  val maxWorkerId = -1L ^ (-1L << workerIdBits)
  val maxClusterId = -1L ^ (-1L << clusterIdBits)
  val workerIdShift = sequenceBits
  val timestampShift = sequenceBits + workerIdBits + clusterIdBits
  val clusterIdShift = sequenceBits + workerIdBits
  val sequenceMask = -1L ^ (-1L << sequenceBits)

  // sanity check for workerId
  if (workerId > maxWorkerId || workerId < 0) {
    throw new IllegalArgumentException("worker Id can't be greater than %d or less than 0".format(maxWorkerId))
  }

  if (clusterId > maxClusterId || clusterId < 0) {
    throw new IllegalArgumentException("cluster Id can't be greater than %d or less than 0".format(maxClusterId))
  }

  def finish(timestamp: Long, timestampShift: Long, clusterId: Long,
    clusterIdShift: Long, workerId: Long, workerIdShift: Long, sequence: Long) = {
    ((timestamp - epoch) << timestampShift) |
      (clusterId << clusterIdShift) |
      (workerId << workerIdShift) |
    sequence
  }

  def receive = {
    case _ ⇒ sender ! next
  }

}

/**
 *  generate a 128 bit snowflake id as a base 62 encoded string.
 */
class BigIntTimestamp(
  val workerId: Long,
  workerIdBits: Long = 48L,
  var sequence: Long = 0L,
  sequenceBits: Long = 16L
) extends Actor with Generator[BigInt] {
  val maxWorkerId = -1L ^ (-1L << workerIdBits)
  val workerIdShift = sequenceBits
  val timestampShift = 64L
  val sequenceMask = -1L ^ (-1L << sequenceBits)
  val clusterId = 0L
  val clusterIdShift = 0L

  def finish(timestamp: Long, timestampShift: Long, clusterId: Long,
    clusterIdShift: Long, workerId: Long, workerIdShift: Long, sequence: Long) =
    (BigInt(timestamp) << timestampShift.toInt) + (BigInt((workerId << workerIdShift) | sequence))

  def receive = {
    case _ ⇒ sender ! next
  }

}

/**
 *  generate a 128 bit snowflake id as a base 62 encoded string.
 */
class StringTimestamp(
  val workerId: Long,
  workerIdBits: Long = 48L,
  var sequence: Long = 0L,
  sequenceBits: Long = 16L
) extends Actor with Generator[String] {
  val maxWorkerId = -1L ^ (-1L << workerIdBits)
  val workerIdShift = sequenceBits
  val timestampShift = 64L
  val sequenceMask = -1L ^ (-1L << sequenceBits)
  val clusterId = 0L
  val clusterIdShift = 0L

  def finish(timestamp: Long, timestampShift: Long, clusterId: Long,
    clusterIdShift: Long, workerId: Long, workerIdShift: Long, sequence: Long) =
    base62((BigInt(timestamp) << timestampShift.toInt) + (BigInt((workerId << workerIdShift) | sequence)))

  def receive = {
    case _ ⇒ sender ! next
  }

}

