package fxsync.push

import java.io.File
import javafx.animation.AnimationTimer
import javafx.application.{Application, Platform}
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.ProgressBar
import javafx.scene.layout.{BorderPane, StackPane, VBox}
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.stage.Stage

import fxsync.util.{FixedFifo, IoMonitor, Lockable}

/**
  * Created by dingb on 2017/7/10.
  */
class PushUi extends Application {
  val CANVAS_WIDTH = 1024
  val CANVAS_HEIGHT = 400
  val borderPane = new BorderPane()
  val vbox = new VBox()
  val graphCanvas = new Canvas()
  graphCanvas.setWidth(CANVAS_WIDTH)
  graphCanvas.setHeight(CANVAS_HEIGHT)
  borderPane.setCenter(vbox)
  val gatheringText = new Text
  val gatheringStatus = new Text("Gathering")
  val gatheringStatusCount = new Lockable[Int](0)
  val gatherProgressBar = new ProgressBar()
  gatherProgressBar.setPrefWidth(1024)
  val processedStatus = new Text("Processed")
  val processedStatusCount = new Lockable[Int](0)

  val processedSending = new Text("Sending")
  val taskUis = for (i<- 0 until PushWorker.config.taskCount) yield new TaskUi

  vbox.getChildren.add(gatheringStatus)
  vbox.getChildren.add(processedStatus)
  vbox.getChildren.add(gatherProgressBar)

  vbox.getChildren.add(gatheringText)

  vbox.getChildren.add(processedSending)
  taskUis.foreach(t=>vbox.getChildren.add(t.root))
  vbox.getChildren.add(graphCanvas)
  val scene = new Scene(borderPane, 1024, 768)

  override def start(primaryStage: Stage): Unit = {
    primaryStage.setScene(scene)
    primaryStage.setTitle("Push")
    PushWorker.doPush(Some(new Listener))
    new GraphTask().start
    primaryStage.show()
  }


  def createTasks: Unit = {
    for(i <- 0 until PushWorker.config.taskCount) {
      val pb = new ProgressBar()
      vbox.getChildren.add(pb)
    }
  }
  class Listener extends PushWorkerListener {
    override def gathered(f: PushItem): Unit = {
      gatheringStatusCount.update(_+1)
      Platform.runLater(()=> {
       gatherProgressBar.setProgress(processedStatusCount.value.toDouble / gatheringStatusCount.value.toDouble)
      })

      Platform.runLater(()=> {
        gatheringStatus.setText("Gathered " + gatheringStatusCount.value + "Files")
        gatheringText.setText(f.vname)
      })
    }

    override def processed(f: PushItem): Unit = {
      processedStatusCount.update(_+1)
      Platform.runLater(()=> {
        gatherProgressBar.setProgress(processedStatusCount.value.toDouble / gatheringStatusCount.value.toDouble)
      })
      Platform.runLater(()=> {
        processedStatus.setText("Processed " + processedStatusCount.value + "Files")
      })
    }

    override def taskCreated(taskId: Int): Unit = {

    }

    override def taskReleased(taskId: Int): Unit = {

    }

    override def taskProgress(taskId: Int, fileName: String, progress: Double): Unit = {
      Platform.runLater(()=> {
        taskUis(taskId).status.setText(new File(fileName).getName)
        taskUis(taskId).progress.setProgress(progress)

      })

    }
  }

  class TaskUi {
    val progress = new ProgressBar()
    val stack = new StackPane()
    val status = new Text
    def root = stack
    progress.setPrefWidth(1024)
    progress.setPrefHeight(24)
    stack.getChildren.add(progress)
    stack.getChildren.add(status)
  }

  class GraphTask {
    val ctx = graphCanvas.getGraphicsContext2D
    val GRID_WIDTH = 32;
    val GRID_COUNT = 1024 / GRID_WIDTH
    val speeds = new FixedFifo[Double](GRID_COUNT, 0)
    var last_net_tx = IoMonitor.get("net.tx")
    var last_update = 0L;
    var last_draw = 0L


    def start = {
      new AnimationTimer(

      ) {
        override def handle(now: Long): Unit = {
          if (now / 1000 / 1000 - last_update / 1000 / 1000  > 1000) {
            last_update = now;
            val current_net_tx = IoMonitor.get("net.tx")
            val speed = (current_net_tx - last_net_tx) / 1024
            speeds.push(speed)
            last_net_tx = current_net_tx
          }

          if (now / 1000 / 1000 - last_draw / 1000 / 1000 > 1000) {
            last_draw = now
            ctx.setFill(Color.BLACK)
            ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT)
            ctx.setStroke(Color.GREEN)


            for (y <- 0 until CANVAS_HEIGHT by GRID_WIDTH) {
              ctx.strokeLine(0, y, CANVAS_WIDTH, y)
            }

            val pixelPerSpeed = if (speeds.max == 0) 0 else (CANVAS_HEIGHT.toDouble / speeds.max.toDouble)
            System.err.println(speeds)
            var x = 0;
            ctx.setFill(new Color(0.0, 0.0, 1.0, 0.5))
            val xPoints: List[Double] = (0.0 :: (0.until(GRID_COUNT).map(_ * GRID_WIDTH.toDouble)).toList) ::: (CANVAS_WIDTH.toDouble :: Nil)
            val yPoints: List[Double] = (CANVAS_HEIGHT.toDouble :: speeds.map(_ * pixelPerSpeed).toList.map(CANVAS_HEIGHT.toDouble - _)) ::: (CANVAS_HEIGHT.toDouble :: Nil)
            println("x" + xPoints)
            println("y" + yPoints)
            ctx.fillPolygon(xPoints.toArray, yPoints.toArray, GRID_COUNT + 2)
          }
        }
      }.start()
    }
  }

}
