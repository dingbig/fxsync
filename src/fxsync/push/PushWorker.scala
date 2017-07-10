package fxsync.push

import fxsync.util.AsyncUtil.async
import fxsync.helper.FileHelper._

/**
  * Created by dingb on 2017/7/10.
  */
object PushWorker {
  var config: PushConfig = null

  def doPush(listener: Option[PushWorkerListener]) = {
    def lis = listener.getOrElse(PushWorkerListener.dummy)
    var scanDone = false
    val items = new scala.collection.mutable.ListBuffer[PushItem]
    async {
      config.sendlist.foreach {
        root =>
          root.forEachFile("/") {
            (vdir, f) =>
              items.synchronized {
                val pi = new PushItem(vdir + "/" + f.getName, f)
                items += pi
                lis.gathered(pi)
              }
          }
      }
      scanDone = true
    }

    async(config.taskCount) {
      taskId =>
        lis.taskCreated(taskId)
        val pc = new PushClient("127.0.0.1", 9696)
        while (!scanDone || items.synchronized(items.length) > 0) {
          items.synchronized {
            if (items.length > 0) {
              Some(items.remove(0))
            } else {
              None
            }
          } match {
            case None =>
            case Some(item) => {
              pc.send(item, taskId, lis)
              lis.processed(item)
            }
          }
        }
        lis.taskReleased(taskId)
        pc.close
        println("pc closed")
    }

  }
}
