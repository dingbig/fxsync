package fxsync.helper

import java.io.{File, FileInputStream, IOException, RandomAccessFile}
import java.security.MessageDigest

/**
  * Created by dingb on 2017/7/10.
  */
object FileHelper {
  implicit def toFileHelper(f: File) = new FileHelper(f)

  class FileHelper(f: File) {
    def eachBuffer(fun: Array[Byte] => Boolean): Unit = f.eachBuffer(0, f.length())(fun)

    def eachBuffer(offset: Long, length: Long)(fun: Array[Byte] => Boolean): Unit = {
      val opt = try {
        Some(new RandomAccessFile(f, "r"))
      } catch {
        case ex: Throwable => None
      }
      opt match {
        case None =>
        case Some(raf) =>
          raf.seek(offset)
          var toRead = length
          var done = false
          val buf = new Array[Byte](1024)
          var userBroken = false
          while (toRead > 0 && !done) {
            val onceRead = if (toRead < 1024) toRead else 1024
            val r = raf.read(buf, 0, onceRead.asInstanceOf[Int])
            if (r <= 0) {
              done = true
            } else {
              userBroken = fun(buf.take(r))
              done = userBroken
              toRead -= r
            }
          }
          if (!userBroken && toRead > 0) {
            throw new IOException("Read broken")
          }
          raf.close()
      }
    }

    def getMD5(): String = {
      val digest = MessageDigest.getInstance("md5")
      if (f.isFile) {
        f.eachBuffer {
          buf => {
            digest.update(buf)
            false
          }
        }
      } else {
        digest.update(f.getAbsolutePath.getBytes)
      }
      digest.digest().map("%02x".format(_)).mkString("")
    }

    def getMD5(progress: Double => Unit): String = {
      val digest = MessageDigest.getInstance("md5")
      if (f.isFile) {
        var total = f.length()
        var updated = 0L
        f.eachBuffer {
          buf => {
            digest.update(buf)
            updated += buf.length
            progress(updated.toDouble / total.toDouble)
            false
          }
        }
      } else {
        digest.update(f.getAbsolutePath.getBytes)
      }
      progress(1)
      digest.digest().map("%02x".format(_)).mkString("")
    }


    def getMD5(size: Long, progress: Double => Unit): String = {
      val digest = MessageDigest.getInstance("md5")
      if (f.isFile) {
        var total = f.length()
        var updated = 0L
        f.eachBuffer(0, size) {
          buf => {
            digest.update(buf)
            updated += buf.length
            progress(updated.toDouble / total.toDouble)
            false
          }
        }
      } else {
        digest.update(f.getAbsolutePath.getBytes)
      }
      progress(1)
      digest.digest().map("%02x".format(_)).mkString("")
    }

    def getMD5(size: Long): String = {
      val digest = MessageDigest.getInstance("md5")
      if (f.isFile) {
        var total = f.length()
        var updated = 0L
        f.eachBuffer(0, size) {
          buf => {
            digest.update(buf)
            updated += buf.length
            false
          }
        }
      } else {
        digest.update(f.getAbsolutePath.getBytes)
      }
      digest.digest().map("%02x".format(_)).mkString("")
    }


    def findFiles(): List[File] = {
      if (f.isDirectory) {
        val subs = f.listFiles()
        if (subs == null) {
          List(f)
        } else {
          f :: subs.flatMap(_.findFiles()).toList
        }
      } else {
        List(f)
      }
    }

    def forEachFile(vdir: String)(func: (String, File) => Unit): Unit = {
      func(vdir, f)
      if (f.isDirectory) {
        val list = f.listFiles()
        if (list != null) {
          list.foreach(x => x.forEachFile(vdir + "/" + f.getName)(func))
        }
      }
    }

    def rmrf(): Unit = {
      println("rmrf " + f.getAbsolutePath)
      if (f.isDirectory) {
        f.listFiles().foreach(_.rmrf())
      }
      f.delete()
    }
  }


}

