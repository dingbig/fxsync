package fxsync.util

import java.net.{ServerSocket, Socket}
import AsyncUtil._

/**
  * Created by dingb on 2017/7/10.
  */
object NetUtil {
  def tcpd(port: Int)(conn: Socket => Unit): Unit = {
    async{
      val ss = new ServerSocket(port)
      while(true) {
        val s = ss.accept()
        async {
          conn(s)
        }
      }
    }
  }
}
