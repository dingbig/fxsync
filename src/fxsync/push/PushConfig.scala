package fxsync.push

import java.io.File

/**
  * Created by dingb on 2017/7/10.
  */
class PushConfig {

  var taskCount: Int = 1
  var host: String = null
  var sendlist = new scala.collection.mutable.ListBuffer[File]

  var ui = true
  def appendFile(f: File) = {
    sendlist += f
    this
  }


  def disableUi() = {
    ui = false
    this
  }

}
