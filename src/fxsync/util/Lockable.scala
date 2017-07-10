package fxsync.util

/**
  * Created by dingb on 2017/7/10.
  */
class Lockable[T](var value: T) {
  private val lock = new Object
  def update(f: T=> T): T = {
    lock.synchronized {
      value = try {
        f(value)
      } catch {
        case ex: Throwable =>
          ex.printStackTrace()
          value
      }
      value
    }
  }
}
