package fxsync.push
import java.io.File
import javafx.application.Application

import fxsync.util.Toolet
import fxsync.util.AsyncUtil._
import fxsync.helper.FileHelper._
import fxsync.mirror.{MirrorDirItem, MirrorItem}

import scala.runtime.Nothing$
/**
  * Created by dingb on 2017/7/9.
  */
class PushToolet extends Toolet {
  override def run(args: String*): Unit = {
    val parser = new scopt.OptionParser[PushConfig]("push") {
      head("push", "3.x")
      opt[String]('h', "host").action( (x, c) => {c.host = x; c}).text("host is the mirror address or domain")
      opt[Int]('t', "task-count").action( (x, c) => {c.taskCount = x; c}).text("tasks count")

      arg[File]("<file>...").unbounded().optional().action((x, c) => c.appendFile(x)).text("files or dirs to send")
      help("help").text("prints this usage text")
      opt[Unit]("no-ui").action( (_, c) =>
        c.disableUi() ).text("disable ui, default is true")
    }
    parser.parse(args, new PushConfig()) match {
      case Some(config) => doPush(config)
      case None =>
    }
  }

  override def help: Unit = {

  }

  override def name: String = "push"

  override def usage: Unit = {
    println("usage: push <mirror ip> <files>")
  }


  def doPush(config: PushConfig): Unit = {
    PushWorker.config = config
    if(config.ui) {
      Application.launch(classOf[PushUi])
    } else {
      PushWorker.doPush(None)
    }
  }
}
