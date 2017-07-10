package fxsync

import javafx.application.Application
import javafx.stage.Stage

import fxsync.mirror.MirrorToolet
import fxsync.pull.PullToolet
import fxsync.push.PushToolet

/**
  * Created by dingb on 2017/7/9.
  */
object main extends App {
  val toolets = List(new MirrorToolet, new PushToolet, new PullToolet)
  if(args.length == 0) {
    usage
  } else {
    toolets.find(_.name == args(0)) match {
      case None => usage("command not found: " + args(0))
      case Some(toolet) => toolet.run(args.drop(1) : _*)
    }
  }



  def usage(msg: String): Unit = {
    println(msg)
    usage
  }
  def usage = {
    println("usage: java -jar fxsync.jar <toolet> [toolet args]")
    printf("where toolet is one of: %s\n", toolets.map(_.name).mkString(" "))
  }

}
