package fxsync.push

import java.io.File


/**
  * Created by dingb on 2017/7/10.
  */
trait PushWorkerListener {
  def gathered(f: PushItem): Unit
  def processed(f: PushItem): Unit
  def taskCreated(taskId: Int)
  def taskReleased(taskId: Int)
  def taskProgress(taskId: Int, fileName: String, progress: Double)
}

object PushWorkerListener {
  def dummy = new PushWorkerListener {
    override def gathered(f: PushItem): Unit = {}
    override def processed(f: PushItem): Unit = {}
    override def taskCreated(taskId: Int): Unit = {}
    override def taskProgress(taskId: Int, fileName: String, progress: Double): Unit = {}
    override def taskReleased(taskId: Int): Unit = {}
  }
}
