package nightcoreplayer

import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.control.Button
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.MouseEvent

object ToolButtonCreator {

  def createButton(imagePath: String, eventHandler: EventHandler[ActionEvent]): Button = {
    val buttonImage = new Image(getClass.getResourceAsStream(imagePath))
    val button      = new Button()
    button.setGraphic(new ImageView(buttonImage))
    button.setStyle("-fx-background-color: Black")
    button.setOnAction(eventHandler)
    button.addEventHandler(MouseEvent.MOUSE_ENTERED, (_: MouseEvent) => button.setStyle("-fx-body-color: Black"))
    button.addEventHandler(MouseEvent.MOUSE_EXITED, (_: MouseEvent) => button.setStyle("-fx-background-color: Black"))
    button
  }

}
