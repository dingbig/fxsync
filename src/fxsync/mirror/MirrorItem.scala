package fxsync.mirror

import java.io.File
import fxsync.helper.FileHelper._
/**
  * Created by dingb on 2017/7/9.
  */
abstract class MirrorItem(serverRoot: File, file: File) {
  def serverPath = file.getAbsolutePath
  def virtualPath = serverPath.substring(serverRoot.getAbsolutePath.length)
  def getMD5 = file.getMD5
}
