package Cassino.GUI

import Cassino.*
import Cassino.io.ReaderWriter

import scala.collection.mutable.Buffer
import scalafx.Includes.*
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.*
import scalafx.event.ActionEvent
import scalafx.geometry.*
import scalafx.scene.Scene
import scalafx.scene.control.*
import scalafx.scene.layout.*
import scalafx.scene.control.Alert.*
import scalafx.scene.image.*
import scalafx.scene.input.MouseEvent

import java.util.NoSuchElementException




object Main extends JFXApp3 {

  var gameOption: Option[Game] = None
  val windowWidth = 1920
  val windowHeight = 1000
  val pCardsHBoxWidth = 1150
  val pCardsHBoxHeight = 225
  val tCardsHBoxWidth = windowWidth
  val tCardsHBoxHeight = 275
  val buttonWidth = 225
  val buttonHeight = 75
  //val buttonHBoxWidth = 3 * buttonWidth + 2 * (buttonWidth / 5)
  //val buttonHBoxHeight = 75
  //val cardsToBeTaken = Buffer[Card]()

  //var cardUsedForTaking: Option[Card] = None
  //var midSelection = false


  def start(): Unit =
    val mainMenu = new PrimaryStage {
        title = "Main Menu"
        scene = new Scene(windowWidth, windowHeight) {
          val anchor = new AnchorPane

          val menuBar  = new MenuBar
          val fileMenu = new Menu("Game File")
          val newFile  = new MenuItem("New")
          val loadFile = new MenuItem("Load")
          val saveFile = new MenuItem("Save")
          fileMenu.items = List(newFile, loadFile, saveFile)
          menuBar.menus = List(fileMenu)
          menuBar.alignmentInParent = Pos.TopCenter
          menuBar.minWidth = windowWidth

          newFile.onAction = (event: ActionEvent) => {
            val players = Buffer[Player]()
            val cDialog = new ChoiceDialog(2, Seq(2,3,4,5)) {
              initOwner(stage)
              title = ""
              headerText = "Create a New Game"
              contentText = "Choose the number of players:"
            }
            val cResult = cDialog.showAndWait()
            cResult match {
              case None         => println("No selection")
              case Some(choice) => {
                val numOfPlayers = choice
                var i = 0
                while i < numOfPlayers do {
                  val tDialog = new TextInputDialog(defaultValue = s"Player ${i}") {
                    initOwner(stage)
                    title = ""
                    headerText = "Create a New Game"
                    contentText = "Please enter a player's name:"
                  }
                  val tResult = tDialog.showAndWait()
                  tResult match {
                    case Some(playerName) => {
                      val ButtonTypeOne = new ButtonType("Human")
                      val ButtonTypeTwo = new ButtonType("Computer")
                      val alert = new Alert(AlertType.Confirmation) {
                        initOwner(stage)
                        title = ""
                        headerText = "Create a New Game"
                        contentText = "Choose whether this player is a human player or a computerized opponent."
                        buttonTypes = Seq(
                          ButtonTypeOne, ButtonTypeTwo, ButtonType.Cancel)
                      }
                      val result = alert.showAndWait()
                      result match {
                        case Some(ButtonTypeOne)   => players += new Human(i, playerName)
                        case Some(ButtonTypeTwo)   => players += new COM(i, playerName)
                        case _ => i = numOfPlayers
                      }
                    }
                    case _ => i = numOfPlayers
                  }
                  i += 1
                }
                if i == numOfPlayers then
                  gameOption = Some(new Game(players))
                  println("Game Created")
                  tCardsHBox.children.clear()
                  pCardsHBox.children.clear()
                  gameOption.get.setupRound()
                  gameInProgress
              }
            }
          }

          loadFile.onAction = (event: ActionEvent) => {
            val tDialog = new TextInputDialog(defaultValue = "CassinoSavefile.txt") {
              initOwner(stage)
              title = ""
              headerText = "Load previously saved game"
              contentText = "Please enter name the of your savefile:"
            }
            val tResult = tDialog.showAndWait()
            tResult match {
              case None       => println("Dialog was canceled.")
              case Some(savefile) => {
                gameOption = Some(ReaderWriter.loadSaveFile(savefile))
              }
            }
            tCardsHBox.children.clear()
            pCardsHBox.children.clear()
            //cardsToBeTaken.clear()
            //cardUsedForTaking = None
            //midSelection = false
            println("Game Loaded")
            println(gameOption.get.players)
            gameInProgress
          }

          saveFile.onAction = (event: ActionEvent) => {
            val tDialog = new TextInputDialog(defaultValue = "CassinoSavefile.txt") {
              initOwner(stage)
              title = ""
              headerText = "Save your Game"
              contentText = "Please enter name the of your savefile:"
            }
            val tResult = tDialog.showAndWait()
            tResult match {
              case None       => println("Dialog was canceled.")
              case Some(fileName) => {
                try
                  ReaderWriter.writeSaveFile(gameOption.get, fileName)
                catch
                  case e:NoSuchElementException => throw NoSuchElementException("No game to save")
              }
            }
          }
          val tCardsHBox = new HBox(5)
          tCardsHBox.minWidth  = tCardsHBoxWidth
          tCardsHBox.minHeight = tCardsHBoxHeight
          tCardsHBox.alignment = Pos.Center

          AnchorPane.setTopAnchor(tCardsHBox, (windowHeight - tCardsHBoxHeight - pCardsHBoxHeight) / 2)
          AnchorPane.setBottomAnchor(tCardsHBox, (windowHeight - tCardsHBoxHeight - pCardsHBoxHeight) / 2 + pCardsHBoxHeight)

          val playButton = new Button("PLAY")
          playButton.minWidth = buttonWidth
          playButton.minHeight = buttonHeight
          playButton.onMouseClicked = (event: MouseEvent) => {
            if gameOption.nonEmpty then
              val game = gameOption.get
              val cardUsedForTaking = game.players(game.turn).cards.filter(_.selected)
              val cardsToBeTaken = game.tableCards.filter(_.selected)
              if !game.playTurn(cardUsedForTaking, cardsToBeTaken) then
                new Alert(AlertType.Information, "Invalid move!").showAndWait()
              updateTableCards(game)
              updatePlayerCards(game)
              //cardsToBeTaken.clear()
              //cardUsedForTaking = None
              //midSelection = false
          }

          AnchorPane.setBottomAnchor(playButton, ((windowHeight - tCardsHBoxHeight - pCardsHBoxHeight) / 2 - buttonHeight) / 3 + pCardsHBoxHeight)
          AnchorPane.setLeftAnchor(playButton, windowWidth / 2 - buttonWidth / 2)
          AnchorPane.setRightAnchor(playButton, windowWidth / 2 - buttonWidth / 2)

          /*val addButton = new Button("ADD")
          addButton.minWidth = buttonWidth
          addButton.minHeight = buttonHBoxHeight
          addButton.onMouseClicked = (event: MouseEvent) => {
            if gameOption.nonEmpty && gameOption.get.players(gameOption.get.turn).cards.exists(_.selected) then
              cardUsedForTaking = gameOption.get.players(gameOption.get.turn).cards.find(_.selected)
              cardsToBeTaken += ((cardUsedForTaking.get, gameOption.get.tableCards.filter(k => k.selected && !cardsToBeTaken.collect(_._2).flatten.contains(k))))
              midSelection = true
          }

          val clearButton = new Button("CLEAR")
          clearButton.minWidth = buttonWidth
          clearButton.minHeight = buttonHBoxHeight
          clearButton.onMouseClicked = (event: MouseEvent) => {
          if gameOption.nonEmpty then
              val game = gameOption.get
              midSelection = false
              if cardUsedForTaking.nonEmpty then
                replaceSelectedPlayerCard(game.players(game.turn).cards.indexOf(cardUsedForTaking.get), game)
                cardUsedForTaking = None
              cardsToBeTaken.collect(_._2).flatten.distinct.foreach(k => replaceSelectedTableCard(game.tableCards.indexOf(k), game))
              cardsToBeTaken.clear()
          }*/

          /*val buttonHBox = new HBox(buttonWidth / 5)
          buttonHBox.minWidth = buttonHBoxWidth
          buttonHBox.minHeight = buttonHBoxHeight
          buttonHBox.alignment = Pos.Center

          AnchorPane.setBottomAnchor(buttonHBox, ((windowHeight - tCardsHBoxHeight - pCardsHBoxHeight) / 2 - buttonHBoxHeight) / 3 + pCardsHBoxHeight)
          AnchorPane.setLeftAnchor(buttonHBox, windowWidth / 2 - buttonHBoxWidth / 2)
          AnchorPane.setRightAnchor(buttonHBox, windowWidth / 2 - buttonHBoxWidth / 2)

          buttonHBox.children = List(addButton, playButton, clearButton)*/

          val pCardsHBox = new HBox(5)
          pCardsHBox.minWidth  = pCardsHBoxWidth
          pCardsHBox.minHeight = pCardsHBoxHeight
          pCardsHBox.alignment = Pos.BottomCenter

          AnchorPane.setBottomAnchor(pCardsHBox, 0)
          AnchorPane.setLeftAnchor(pCardsHBox, 385)
          AnchorPane.setRightAnchor(pCardsHBox, 385)

          anchor.children += menuBar
          anchor.children += tCardsHBox
          anchor.children += playButton
          anchor.children += pCardsHBox

          root = anchor


          def gameInProgress: Unit =
            if gameOption.nonEmpty then
              val game = gameOption.get
              updatePlayerCards(game)
              updateTableCards(game)

          def updatePlayerCards(game: Game) =
            pCardsHBox.children.clear()
            game.players(game.turn).cards.foreach(k => {
                val img = new Image(s"file:cards/${k.toString.toLowerCase.replace(' ', '_')}.png", 150, pCardsHBoxHeight, true, true)
                pCardsHBox.children += new ImageView(img)
                })
              pCardsHBox.children.foreach(k => k.onMouseClicked = (event: MouseEvent) => replaceSelectedPlayerCard(pCardsHBox.children.indexOf(k), game))

          def updateTableCards(game: Game) =
            tCardsHBox.children.clear()
            game.tableCards.foreach(k => tCardsHBox.children += getImageView(k, 175, tCardsHBoxHeight))
              tCardsHBox.children.foreach(k => k.onMouseClicked = (event: MouseEvent) => replaceSelectedTableCard(tCardsHBox.children.indexOf(k), game))

          def replaceSelectedPlayerCard(index: Int, game: Game): Unit =
            //if !midSelection then
            val card = game.players(game.turn).cards(index)
            val img  = new Image(s"file:cards/${game.players(game.turn % game.players.size).cards(index).toString.toLowerCase.replace(' ', '_')}.png", 150, pCardsHBoxHeight, true, true)
            var writableImg = new WritableImage(img.pixelReader.get, img.width.value.toInt, img.height.value.toInt)
            val pWriter = writableImg.pixelWriter
            if card.selected then
                card.selectToggle
            else if game.players(game.turn).cards.forall(!_.selected) then
              for i <- 0 until img.width.value.toInt; j <- 0 until img.height.value.toInt do
                pWriter.setArgb(i, j,img.pixelReader.get.getArgb(i,j) -0xA0000000)
              card.selectToggle
            val newImageView = new ImageView(writableImg)
            newImageView.onMouseClicked = (event: MouseEvent) => {
                replaceSelectedPlayerCard(index, game)}
            pCardsHBox.children.update(index, newImageView)


          def replaceSelectedTableCard(index: Int, game: Game): Unit =
            val card = game.tableCards(index)
            val img  = new Image(s"file:cards/${game.tableCards(index).toString.toLowerCase.replace(' ', '_')}.png", 175, tCardsHBoxHeight, true, true)
            var writableImg = new WritableImage(img.pixelReader.get, img.width.value.toInt, img.height.value.toInt)
            val pWriter = writableImg.pixelWriter
            val newImageView = new ImageView(writableImg)
            if !card.selected then
              for i <- 0 until img.width.value.toInt; j <- 0 until img.height.value.toInt do
                pWriter.setArgb(i, j,img.pixelReader.get.getArgb(i,j) -0xA0000000)
            newImageView.onMouseClicked = (event: MouseEvent) => {
                replaceSelectedTableCard(index, game)}
            tCardsHBox.children.update(index, newImageView)
            card.selectToggle


          def getImageView(card: Card, width: Double, height: Double, preserveRatio: Boolean = true, smooth: Boolean = true): ImageView =
            new ImageView(new Image(s"file:cards/${card.toString.toLowerCase.replace(' ', '_')}.png", width, height, preserveRatio, smooth))
        }
    }
}