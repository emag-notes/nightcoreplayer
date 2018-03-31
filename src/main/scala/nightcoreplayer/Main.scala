package nightcoreplayer

import java.io.File

import javafx.application.Application
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control._
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.{DragEvent, MouseEvent, TransferMode}
import javafx.scene.layout.{BorderPane, HBox}
import javafx.scene.media.{Media, MediaPlayer, MediaView}
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.util.Duration

object Main extends App {
  Application.launch(classOf[Main], args: _*)
}

class Main extends Application {

  private[this] val mediaViewFitWidth  = 800
  private[this] val mediaViewFitHeight = 450
  private[this] val toolBarMinHeight   = 50
  private[this] val tableMinWidth      = 300

  override def start(primaryStage: Stage): Unit = {
    val mediaView = new MediaView()

    val timeLabel = new Label()
    timeLabel.setTextFill(Color.WHITE)

    val toolBar = new HBox()
    toolBar.setMinHeight(toolBarMinHeight)
    toolBar.setAlignment(Pos.CENTER)
    toolBar.setStyle("-fx-background-color: Black")

    val tableView = new TableView[Movie]()
    tableView.setMinWidth(tableMinWidth)
    val movies = FXCollections.observableArrayList[Movie]()
    tableView.setItems(movies)
    tableView.setRowFactory((_: TableView[Movie]) => {
      val row = new TableRow[Movie]()
      row.setOnMouseClicked((event: MouseEvent) => {
        if (event.getClickCount >= 1 && !row.isEmpty) {
          playMovie(row.getItem, tableView, mediaView, timeLabel)
        }
      })
      row
    })

    val fileNameColumn = new TableColumn[Movie, String]("ファイル名")
    fileNameColumn.setCellValueFactory(new PropertyValueFactory("fileName"))
    fileNameColumn.setPrefWidth(160)
    val timeColumn = new TableColumn[Movie, String]("時間")
    timeColumn.setCellValueFactory(new PropertyValueFactory("time"))
    timeColumn.setPrefWidth(80)
    val deleteActionColumn = new TableColumn[Movie, Long]("削除")
    deleteActionColumn.setCellValueFactory(new PropertyValueFactory("id"))
    deleteActionColumn.setPrefWidth(60)
    deleteActionColumn.setCellFactory((_: TableColumn[Movie, Long]) => {
      new DeleteCell(movies, mediaView, tableView)
    })
    tableView.getColumns.setAll(fileNameColumn, timeColumn, deleteActionColumn)

    // first button
    val firstButtonImage = new Image(getClass.getResourceAsStream("first.png"))
    val firstButton      = new Button()
    firstButton.setGraphic(new ImageView(firstButtonImage))
    firstButton.setStyle("-fx-background-color: Black")
    firstButton.setOnAction((_: ActionEvent) => {
      if (mediaView.getMediaPlayer != null) {
        playPre(tableView, mediaView, timeLabel)
      }
    })
    firstButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                                (_: MouseEvent) => firstButton.setStyle("-fx-body-color: Black"))
    firstButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                                (_: MouseEvent) => firstButton.setStyle("-fx-background-color: Black"))

    // back button
    val backButtonImage = new Image(getClass.getResourceAsStream("back.png"))
    val backButton      = new Button()
    backButton.setGraphic(new ImageView(backButtonImage))
    backButton.setStyle("-fx-background-color: Black")
    backButton.setOnAction((_: ActionEvent) => {
      if (mediaView.getMediaPlayer != null) {
        mediaView.getMediaPlayer.seek(
          mediaView.getMediaPlayer.getCurrentTime.subtract(new Duration(10000))
        )
      }
    })
    backButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                               (_: MouseEvent) => backButton.setStyle("-fx-body-color: Black"))
    backButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                               (_: MouseEvent) => backButton.setStyle("-fx-background-color: Black"))

    // play button
    val playButtonImage = new Image(getClass.getResourceAsStream("play.png"))
    val playButton      = new Button()
    playButton.setGraphic(new ImageView(playButtonImage))
    playButton.setStyle("-fx-background-color: Black")
    playButton.setOnAction((_: ActionEvent) => {
      val selectionModel = tableView.getSelectionModel
      if (mediaView.getMediaPlayer != null && !selectionModel.isEmpty) {
        mediaView.getMediaPlayer.play()
      }
    })
    playButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                               (_: MouseEvent) => playButton.setStyle("-fx-body-color: Black"))
    playButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                               (_: MouseEvent) => playButton.setStyle("-fx-background-color: Black"))

    // pause button
    val pauseButtonImage = new Image(getClass.getResourceAsStream("pause.png"))
    val pauseButton      = new Button()
    pauseButton.setGraphic(new ImageView(pauseButtonImage))
    pauseButton.setStyle("-fx-background-color: Black")
    pauseButton.setOnAction((_: ActionEvent) => if (mediaView.getMediaPlayer != null) mediaView.getMediaPlayer.pause())
    pauseButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                                (_: MouseEvent) => pauseButton.setStyle("-fx-body-color: Black"))
    pauseButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                                (_: MouseEvent) => pauseButton.setStyle("-fx-background-color: Black"))

    // forward button
    val forwardButtonImage = new Image(getClass.getResourceAsStream("forward.png"))
    val forwardButton      = new Button()
    forwardButton.setGraphic(new ImageView(forwardButtonImage))
    forwardButton.setStyle("-fx-background-color: Black")
    forwardButton.setOnAction((_: ActionEvent) => {
      if (mediaView.getMediaPlayer != null) {
        mediaView.getMediaPlayer.seek(
          mediaView.getMediaPlayer.getCurrentTime.add(new Duration(10000))
        )
      }
    })
    forwardButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                                  (_: MouseEvent) => forwardButton.setStyle("-fx-body-color: Black"))
    forwardButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                                  (_: MouseEvent) => forwardButton.setStyle("-fx-background-color: Black"))

    // last button
    val lastButtonImage = new Image(getClass.getResourceAsStream("last.png"))
    val lastButton      = new Button()
    lastButton.setGraphic(new ImageView(lastButtonImage))
    lastButton.setStyle("-fx-background-color: Black")
    lastButton.setOnAction((_: ActionEvent) => {
      if (mediaView.getMediaPlayer != null) {
        playNext(tableView, mediaView, timeLabel)
      }
    })
    lastButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                               (_: MouseEvent) => lastButton.setStyle("-fx-body-color: Black"))
    lastButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                               (_: MouseEvent) => lastButton.setStyle("-fx-background-color: Black"))

    // fullscreen button
    val fullScreenImage  = new Image(getClass.getResourceAsStream("fullscreen.png"))
    val fullScreenButton = new Button()
    fullScreenButton.setGraphic(new ImageView(fullScreenImage))
    fullScreenButton.setStyle("-fx-background-color: Black")
    fullScreenButton.setOnAction((_: ActionEvent) => primaryStage.setFullScreen(true))
    fullScreenButton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                                     (_: MouseEvent) => fullScreenButton.setStyle("-fx-body-color: Black"))
    fullScreenButton.addEventHandler(MouseEvent.MOUSE_EXITED,
                                     (_: MouseEvent) => fullScreenButton.setStyle("-fx-background-color: Black"))

    toolBar.getChildren.addAll(firstButton, backButton, playButton, pauseButton, forwardButton, lastButton, fullScreenButton, timeLabel)

    val baseBorderPane = new BorderPane()
    baseBorderPane.setStyle("-fx-background-color: Black")
    baseBorderPane.setCenter(mediaView)
    baseBorderPane.setBottom(toolBar)
    baseBorderPane.setRight(tableView)

    val scene = new Scene(baseBorderPane, mediaViewFitWidth + tableMinWidth, mediaViewFitHeight + toolBarMinHeight)
    scene.setFill(Color.BLACK)
    mediaView.fitWidthProperty().bind(scene.widthProperty().subtract(tableMinWidth))
    mediaView.fitHeightProperty().bind(scene.heightProperty())

    scene.setOnDragOver((event: DragEvent) => {
      if (event.getGestureSource != scene &&
          event.getDragboard.hasFiles) {
        event.acceptTransferModes(TransferMode.COPY_OR_MOVE: _*)
      }
      event.consume()
    })

    scene.setOnDragDropped((event: DragEvent) => {
      val db = event.getDragboard
      if (db.hasFiles) {
        db.getFiles.toArray(Array[File]()).toSeq.foreach { f =>
          val filePath = f.getAbsolutePath
          val fileName = f.getName
          val media    = new Media(f.toURI.toString)
          val player   = new MediaPlayer(media)
          player.setOnReady(() => {
            val time  = formatTime(media.getDuration)
            val movie = Movie(System.currentTimeMillis(), fileName, time, filePath, media)
            while (movies.contains(movie)) {
              movie.setId(movie.getId + 1L)
            }
            movies.add(movie)
            player.dispose()
          })
        }
      }
      event.consume()
    })

    primaryStage.setTitle("mp4 ファイルをドラッグ & ドロップしてください")

    primaryStage.setScene(scene)
    primaryStage.show()
  }

  private def playMovie(movie: Movie, tableView: TableView[Movie], mediaView: MediaView, timeLabel: Label): Unit = {
    if (mediaView.getMediaPlayer != null) {
      val oldPlayer = mediaView.getMediaPlayer
      oldPlayer.stop()
      oldPlayer.dispose()
    }

    val mediaPlayer = new MediaPlayer(movie.media)
    mediaPlayer
      .currentTimeProperty()
      .addListener(new ChangeListener[Duration] {
        override def changed(observable: ObservableValue[_ <: Duration],
                             oldValue: Duration,
                             newValue: Duration): Unit = {
          timeLabel.setText(formatTime(mediaPlayer.getCurrentTime, mediaPlayer.getTotalDuration))
        }
      })
    mediaPlayer.setOnReady(
      () => timeLabel.setText(formatTime(mediaPlayer.getCurrentTime, mediaPlayer.getTotalDuration))
    )

    mediaPlayer.setOnEndOfMedia(() => playNext(tableView, mediaView, timeLabel))

    mediaView.setMediaPlayer(mediaPlayer)
    mediaPlayer.setRate(1.25)
    mediaPlayer.play()
  }

  private[this] def playPre(tableView: TableView[Movie], mediaView: MediaView, timeLabel: Label): Unit = {
    val selectionModel = tableView.getSelectionModel
    if (selectionModel.isEmpty) return

    val index    = selectionModel.getSelectedIndex
    val preIndex = (tableView.getItems.size() + index - 1) % tableView.getItems.size()
    selectionModel.select(preIndex)
    val movie = selectionModel.getSelectedItem
    playMovie(movie, tableView, mediaView, timeLabel)
  }

  private[this] def playNext(tableView: TableView[Movie], mediaView: MediaView, timeLabel: Label): Unit = {
    val selectionModel = tableView.getSelectionModel
    if (selectionModel.isEmpty) return

    val index     = selectionModel.getSelectedIndex
    val nextIndex = (index + 1) % tableView.getItems.size()
    selectionModel.select(nextIndex)
    val movie = selectionModel.getSelectedItem
    playMovie(movie, tableView, mediaView, timeLabel)
  }

  private[this] def formatTime(elapsed: Duration): String =
    "%02d:%02d:%02d".format(
      elapsed.toHours.toInt,
      elapsed.toMinutes.toInt % 60,
      elapsed.toSeconds.toInt % 60
    )

  private[this] def formatTime(elapsed: Duration, duration: Duration): String =
    s"${formatTime(elapsed)}/${formatTime(duration)}"

}
