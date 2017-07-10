package fxsync.push
import java.io.{DataInputStream, DataOutputStream}
import java.net.Socket

import fxsync.helper.FileHelper._
import fxsync.util.IoMonitor
/**
  * Created by dingb on 2017/7/10.
  */
class PushClient(host: String, port: Int) {
  val socket = new Socket(host, port)
  val dis = new DataInputStream(socket.getInputStream)
  val dos = new DataOutputStream(socket.getOutputStream)

  def send(item: PushItem, taskId: Int, lis: PushWorkerListener): Unit = {
    //send remote
    dos.writeUTF(item.vtype)
    dos.writeUTF(item.vname)
    dos.writeLong(item.localFile.length()) //client_size
    dos.flush()

    //wait remote
    val remote_size = dis.readLong()
    val remote_md5 = dis.readUTF()

    val local_md5 = item.localFile.getMD5(remote_size,
      progress =>
        lis.taskProgress(taskId, item.vname, progress)
    )

    IoMonitor.increase("fs.rx", remote_size)

    val client_skip = if(item.isDir) {
      -1L
    } else if(remote_md5 != local_md5){
      0L
    } else {
      item.localFile.length()
    }

    //sent remote
    dos.writeLong(client_skip) //client_skip
    dos.flush()
    if(client_skip >= 0) {
      var total = item.localFile.length() - client_skip;
      var sent = 0L

      item.localFile.eachBuffer(client_skip, total) {
        buf =>
          sent += buf.length
          dos.write(buf)
          IoMonitor.increase("fs.rx", buf.length)
          IoMonitor.increase("net.tx", buf.length)

          lis.taskProgress(taskId, item.vname, sent.toDouble / total.toDouble)
          false
      }
      dos.flush()
    }
    dos.flush()
  }
  def close = {
    dis.close()
    dos.close()
    socket.close()
  }
}
