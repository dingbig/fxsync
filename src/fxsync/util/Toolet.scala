package fxsync.util

/**
  * Created by dingb on 2017/7/9.
  */
abstract class Toolet {
  def name: String
  def run(args: String*): Unit
  def help
  def usage
}
