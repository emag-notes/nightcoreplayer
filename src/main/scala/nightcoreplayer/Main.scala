package nightcoreplayer

import java.io.File

import javafx.application.Application
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.FXCollections
import javafx.event.{ActionEvent, EventHandler}
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
    val firstButton = createButton("first.png",
                                   (_: ActionEvent) =>
                                     if (mediaView.getMediaPlayer != null) {
                                       playPre(tableView, mediaView, timeLabel)
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
          mediaView.getMediaPlayer.seek(
            mediaView.getMediaPlayer.getCurrentTime.add(new Duration(10000))
          )
        }
      }
    )

    // last button
    val lastButton = createButton("last.png", (_: ActionEvent) => {
      if (mediaView.getMediaPlayer != null) {
        playNext(tableView, mediaView, timeLabel)
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

    val baseBorderPane = new BorderPane()
    baseBorderPane.setStyle("-fx-background-color: Black")
    baseBorderPane.setCenter(mediaView)
    baseBorderPane.setBottom(toolBar)
    baseBorderPane.setRight(tableView)

    val scene = new Scene(baseBorderPane, mediaViewFitWidth + tableMinWidth, mediaViewFitHeight + toolBarMinHeight)
    scene.setFill(Color.BLACK)
    mediaView.fitWidthProperty().bind(scene.widthProperty().subtract(tableMinWidth))
    mediaView.fitHeightProperty().bind(scene.heightProperty())

    scene.setOnDragOver(new MovieFileDragOverEventHandler(scene))
    scene.setOnDragDropped(new MovieFileDragDroppedEventHandler(movies))

    primaryStage.setTitle("mp4 ファイルをドラッグ & ドロップしてください")

    primaryStage.setScene(scene)
    primaryStage.show()
  }

  private[this] def createButton(imagePath: String, eventHandler: EventHandler[ActionEvent]): Button = {
    val buttonImage = new Image(getClass.getResourceAsStream(imagePath))
    val button      = new Button()
    button.setGraphic(new ImageView(buttonImage))
    button.setStyle("-fx-background-color: Black")
    button.setOnAction(eventHandler)
    button.addEventHandler(MouseEvent.MOUSE_ENTERED, (_: MouseEvent) => button.setStyle("-fx-body-color: Black"))
    button.addEventHandler(MouseEvent.MOUSE_EXITED, (_: MouseEvent) => button.setStyle("-fx-background-color: Black"))
    button
  }

  private[this] def playMovie(movie: Movie,
                              tableView: TableView[Movie],
                              mediaView: MediaView,
                              timeLabel: Label): Unit = {
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

  sealed trait Track
  object Pre  extends Track
  object Next extends Track

  private[this] def playAt(track: Track, tableView: TableView[Movie], mediaView: MediaView, timeLabel: Label): Unit = {
    val selectionModel = tableView.getSelectionModel
    if (selectionModel.isEmpty) return

    val index = selectionModel.getSelectedIndex
    val changedIndex = track match {
      case Pre  => (tableView.getItems.size() + index - 1) % tableView.getItems.size()
      case Next => (index + 1)                             % tableView.getItems.size()
    }
    selectionModel.select(changedIndex)
    val movie = selectionModel.getSelectedItem
    playMovie(movie, tableView, mediaView, timeLabel)
  }

  private[this] def playPre(tableView: TableView[Movie], mediaView: MediaView, timeLabel: Label): Unit = {
    playAt(Pre, tableView, mediaView, timeLabel)
  }

  private[this] def playNext(tableView: TableView[Movie], mediaView: MediaView, timeLabel: Label): Unit = {
    playAt(Next, tableView, mediaView, timeLabel)
  }

}
