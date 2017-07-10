package fxsync.mirror

import java.io.{DataInputStream, DataOutputStream, File, RandomAccessFile}
import java.net.Socket

import scala.collection.mutable
import fxsync.helper.FileHelper._
import fxsync.util.NetUtil._

/**
  * Created by dingb on 2017/7/9.
  */
class MirrorServer(mirrotPath: String, port: Int) {
  val items = new mutable.HashMap[String, MirrorItem]()
  val path = new File((mirrotPath))
  path.findFiles.map {
    f => if (f.isDirectory) new MirrorDirItem(path, f) else new MirrorFileItem(path, f)
  }.foreach(i => items.put(i.virtualPath, i))
  items.foreach {
    i => println(i._1, i._2.getMD5)
  }
  tcpd(9696) {
    s =>
      println(s.getRemoteSocketAddress)
      var done = false
      while (!done) {
        try {
          waitFile(s)
        } catch {
          case ex: Throwable =>
            println(ex.getMessage)
            done = true
        }
      }
      s.close()
  }
  def waitFile(s: Socket): Unit = {
    val dis = new DataInputStream(s.getInputStream)
    val dos = new DataOutputStream(s.getOutputStream)
    //wait client
    val vtype = dis.readUTF()
    val vname = dis.readUTF()
    val client_size = dis.readLong()
    val serverFile = new File(mirrotPath, vname)
    println(vtype, vname, "server:", serverFile)
    println("client_size:", client_size)
      //send to remote
    val server_md5 = serverFile.getMD5(client_size)
    dos.writeLong(serverFile.length)
    dos.writeUTF(server_md5)
    dos.flush()
    //wait remote
    val client_skip = dis.readLong()
    if (vtype == "d") {
      if (!serverFile.isDirectory) {
        serverFile.rmrf
      }
      serverFile.mkdirs()
    } else {
      if (client_size == 0) {
        if (!serverFile.exists()) {
        } else if (serverFile.isDirectory) {
          serverFile.rmrf()
        } else if (serverFile.length() > 0) {
          serverFile.rmrf()
        }
        serverFile.getParentFile.mkdirs()
        serverFile.createNewFile()
      } else {
        serverFile.getParentFile.mkdirs()
        val raf = new RandomAccessFile(serverFile, "rw")
        raf.seek(client_skip)
        var toRead = client_size - client_skip
        val buf = new Array[Byte](1024)
        var done = false
        println("toRead " + toRead)
        while (toRead > 0 && !done) {
          val onceRead = if (toRead < 1024) toRead else 1024
          val r = dis.read(buf, 0, onceRead.asInstanceOf[Int])
          if (r <= 0) {
            done = true
          } else {
            toRead -= r
            raf.write(buf, 0, r)
          }
        }
        raf.close()
        if (toRead > 0) {
          println("read broken")
          serverFile.delete()
        }
      }
    }
  }
}
