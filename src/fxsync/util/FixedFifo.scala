package fxsync.util

import scala.collection.mutable.ListBuffer

/**
  * Created by dingb on 2017/7/10.
  */
class FixedFifo[T](size: Int, blankValue: T) extends scala.collection.Seq[T] {
  private val buf = new ListBuffer[T]
  for(i <- 0 until size) {
    buf += blankValue
  }
  def push(o: T): Unit = {
    buf.synchronized {
      buf += o
      buf.remove(0)
    }
  }

  override def length: Int = buf.synchronized(buf.size)
  override def apply(idx: Int): T = buf.synchronized(buf(idx))

  override def iterator: Iterator[T] = new Iterator[T]() {
    var pos = 0
    override def hasNext: Boolean = buf.synchronized(pos < buf.size)
    override def next(): T = {
      buf.synchronized {
        val o = buf(pos)
        pos += 1;
        o
      }
    }
  }
}
