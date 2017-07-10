package fxsync.util

import scala.collection.mutable

/**
  * Created by dingb on 2017/7/10.
  */
object IoMonitor {
  val totals = new mutable.HashMap[String, Long]()
  def increase(name: String, value: Long): Unit = {
    totals.synchronized {
      totals.get(name) match {
        case None => totals.put(name, 0)
        case Some(v) =>
          totals.put(name, v + value)
      }
    }
  }
  def get(name: String) : Long= {
    totals.synchronized {
      if(totals.contains(name)) {
        totals.get(name).get
      } else {
        0L
      }
    }
  }
}
