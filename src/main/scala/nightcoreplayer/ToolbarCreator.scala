package nightcoreplayer

import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.control.{Label, TableView}
import javafx.scene.layout.HBox
import javafx.scene.media.MediaView
import javafx.stage.Stage
import javafx.util.Duration
import nightcoreplayer.SizeConstants._
import nightcoreplayer.ToolButtonCreator.createButton

object ToolbarCreator {

  def create(mediaView: MediaView, tableView: TableView[Movie], timeLabel: Label, primaryStage: Stage): HBox = {
    val toolBar = new HBox()
    toolBar.setMinHeight(toolBarMinHeight)
    toolBar.setAlignment(Pos.CENTER)
    toolBar.setStyle("-fx-background-color: Black")

    // first button
    val firstButton = createButton("first.png",
                                   (_: ActionEvent) =>
                                     if (mediaView.getMediaPlayer != null) {
                                       MoviePlayer.playPre(tableView, mediaView, timeLabel)
                                   })

    // back button
    val backButton = createButton(
      "back.png",
      (_: ActionEvent) => {
        if (mediaView.getMediaPlayer != null) {
          mediaView.getMediaPlayer.seek(
            mediaView.getMediaPlayer.getCurrentTime.subtract(new Duration(10000))
          )
        }
      }
    )

    // play button
    val playButton = createButton(
      "play.png",
      (_: ActionEvent) => {
        val selectionModel = tableView.getSelectionModel
        if (mediaView.getMediaPlayer != null && !selectionModel.isEmpty) {
          mediaView.getMediaPlayer.play()
        }
      }
    )

    // pause button
    val pauseButton = createButton(
      "pause.png",
      (_: ActionEvent) => if (mediaView.getMediaPlayer != null) mediaView.getMediaPlayer.pause()
    )

    // forward button
    val forwardButton = createButton(
      "forward.png",
      (_: ActionEvent) => {
        if (mediaView.getMediaPlayer != null) {
          val player = mediaView.getMediaPlayer
          if (player.getTotalDuration.greaterThan(player.getCurrentTime.add(new Duration(10000)))) {
            player.seek(player.getCurrentTime.add(new Duration(10000)))
          }
        }
      }
    )

    // last button
    val lastButton = createButton("last.png", (_: ActionEvent) => {
      if (mediaView.getMediaPlayer != null) {
        MoviePlayer.playNext(tableView, mediaView, timeLabel)
      }
    })

    // fullscreen button
    val fullScreenButton = createButton("fullscreen.png", (_: ActionEvent) => primaryStage.setFullScreen(true))

    toolBar.getChildren.addAll(firstButton,
                               backButton,
                               playButton,
                               pauseButton,
                               forwardButton,
                               lastButton,
                               fullScreenButton,
                               timeLabel)

    toolBar
  }

}
