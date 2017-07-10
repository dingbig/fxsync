package fxsync.push

import java.io.File

/**
  * Created by dingb on 2017/7/10.
  */
class PushItem(_vfile: String, val localFile: File) {
  val vname = _vfile.replace("\\","/").replace("//","/").replace("//", "/")
  def isDir = localFile.isDirectory
  def isFile = localFile.isFile
  def vtype = if(isDir) "d" else  "f"

}
