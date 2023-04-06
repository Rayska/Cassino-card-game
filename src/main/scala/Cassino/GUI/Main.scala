package Cassino.GUI

import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.*
import scalafx.scene.Scene
import scalafx.scene.control.*
import scalafx.Includes.*
import scalafx.event.ActionEvent



object Main extends JFXApp3 {

  def start(): Unit =
    stage = new PrimaryStage {
      title = "Cassino"
      scene = new Scene(600, 450) {
        val buttonNewGame  = new Button("New Game")
        buttonNewGame.layoutX = 100
        buttonNewGame.layoutY = 50
        buttonNewGame.onAction = (event: ActionEvent) => println("New Game created")

        val buttonLoadGame = new Button("Load Game")
        buttonLoadGame.layoutX = 200
        buttonLoadGame.layoutY = 100
        buttonLoadGame.onAction = (event: ActionEvent) => println("Game Loaded")

        content = List(buttonNewGame, buttonLoadGame)
      }
    }


  /*def start(): Unit =

    /*
    Creation of a new primary stage (Application window).
    We can use Scala's anonymous subclass syntax to get quite
    readable code.
    */

    stage = new JFXApp3.PrimaryStage:
      title = "Hello Stage"
      width = 600
      height = 450

    /*
    Create root gui component, add it to a Scene
    and set the current window scene.
    */

    val root = new AnchorPane()
    val scene = Scene(parent = root) // Scene acts as a container for the scene graph
    stage.scene = scene // Assigning the new scene as the current scene for the stage

    val newGameButton = Button("New Game")
    newGameButton.layoutX = 300
    newGameButton.layoutY = 200

    val loadGameButton = Button("Load Game")
    loadGameButton.layoutX = 300
    loadGameButton.layoutY = 250

    val connHBox = new VBox()
    connHBox.setAlignment(Pos.Center)
    connHBox.getChildren.addAll(newGameButton, loadGameButton)
    connHBox.setSpacing(30)
    connHBox.setAlignment(Pos.Center)

    root.getChildren().add(connHBox)*/


}
