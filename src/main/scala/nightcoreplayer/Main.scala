package nightcoreplayer

import java.nio.file.Paths

import javafx.application.Application
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.{BorderPane, HBox}
import javafx.scene.media.{Media, MediaPlayer, MediaView}
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.util.Duration

object Main extends App {
  Application.launch(classOf[Main], args: _*)
}

class Main extends Application {

  override def start(primaryStage: Stage): Unit = {
    val path        = Paths.get(System.getProperty("user.home"), "Downloads", "myvideo.mp4")
    val media       = new Media(path.toUri.toString)
    val mediaPlayer = new MediaPlayer(media)
    mediaPlayer.setRate(1.25)
    mediaPlayer.play()
    val mediaView = new MediaView(mediaPlayer)
    mediaView.setFitWidth(800)
    mediaView.setFitHeight(450)

    val timeLabel = new Label()
    mediaPlayer
      .currentTimeProperty()
      .addListener(new ChangeListener[Duration] {
        override def changed(observable: ObservableValue[_ <: Duration], oldValue: Duration, newValue: Duration): Unit =
          timeLabel.setText(formatTime(mediaPlayer.getCurrentTime, mediaPlayer.getTotalDuration))
      })
    mediaPlayer.setOnReady(
      () => timeLabel.setText(formatTime(mediaPlayer.getCurrentTime, mediaPlayer.getTotalDuration))
    )
    timeLabel.setTextFill(Color.WHITE)
    val toolBar = new HBox(timeLabel)
    toolBar.setAlignment(Pos.CENTER)
    toolBar.setStyle("-fx-background-colr: Black")

    val baseBorderPane = new BorderPane()
    baseBorderPane.setStyle("-fx-background-color: Black")
    baseBorderPane.setCenter(mediaView)
    baseBorderPane.setBottom(toolBar)

    val scene = new Scene(baseBorderPane, 800, 500)
    scene.setFill(Color.BLACK)

    primaryStage.setScene(scene)
    primaryStage.show()
  }

  private[this] def formatTime(elapsed: Duration, duration: Duration): String =
    "%02d:%02d:%02d/%02d:%02d:%02d".format(
      elapsed.toHours.toInt,
      elapsed.toMinutes.toInt % 60,
      elapsed.toSeconds.toInt % 60,
      duration.toHours.toInt,
      duration.toMinutes.toInt % 60,
      duration.toSeconds.toInt % 60
    )

}
