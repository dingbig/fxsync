package fxsync.mirror

import fxsync.util.Toolet

/**
  * Created by dingb on 2017/7/9.
  */
class MirrorToolet extends Toolet {
  override def run(args: String*): Unit = {
    if(args.length == 0) {
      usage
    } else {
      new MirrorServer(args(0), 9696)
    }
  }

  override def help: Unit = ???

  override def name: String =  "mirror"

  override def usage: Unit = {
    println("mirrot <folder>")
  }
}
