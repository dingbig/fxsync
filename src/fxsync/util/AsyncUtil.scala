package fxsync.util

/**
  * Created by dingb on 2017/7/10.
  */
object AsyncUtil {
  def async(proc: => Unit): Unit = {
    new Thread(()=>proc).start()
  }
  def async(count: Int)(proc: Int=> Unit): Unit = {
    for (i <- 0 until count) {
      new Thread(() => proc(i)).start()
    }
  }

}
