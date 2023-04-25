package Cassino.GUI

import Cassino.*
import Cassino.io.{CorruptedCassinoFileException, ReaderWriter}

import scala.collection.mutable.Buffer
import scalafx.Includes.*
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.*
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.event.ActionEvent
import scalafx.geometry.*
import scalafx.scene.Scene
import scalafx.scene.control.*
import scalafx.scene.layout.*
import scalafx.scene.control.Alert.*
import scalafx.scene.image.*
import scalafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import scalafx.scene.text.*
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.TableView.ResizeFeatures

import java.util.NoSuchElementException

object Main extends JFXApp3 {

  var gameOption: Option[Game] = None
  //var scores: Option[Buffer[(Player, Int)]] = None
  val windowWidth = 1920
  val windowHeight = 1000
  val pCardsHBoxWidth = windowWidth * 0.6
  val pCardsHBoxHeight = windowHeight * 0.23
  val ScrollerWidth = windowWidth
  val scrollerHeight = 275
  val buttonWidth = 225
  val buttonHeight = 75
  val tableCardWidth = 165
  val tableCardHeight = scrollerHeight
  val playerCardWidth = 150
  val playerCardHeight = pCardsHBoxHeight

  //val buttonHBoxWidth = 3 * buttonWidth + 2 * (buttonWidth / 5)
  //val buttonHBoxHeight = 75
  //val cardsToBeTaken = Buffer[Card]()

  //var cardUsedForTaking: Option[Card] = None
  //var midSelection = false


  def start(): Unit =
    val mainMenu = new PrimaryStage {
        title = "Main Menu"
        scene = new Scene(windowWidth, windowHeight) {
          val stack = new StackPane()

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
                try
                  gameOption = Some(ReaderWriter.loadSaveFile(savefile))
                catch
                  case e:CorruptedCassinoFileException => new Alert(AlertType.Information, e.toString).showAndWait()
              }
            }
            if gameOption.nonEmpty then
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
          tCardsHBox.minWidth  = ScrollerWidth
          tCardsHBox.minHeight = scrollerHeight
          tCardsHBox.alignment = Pos.Center

          //AnchorPane.setTopAnchor(tCardsHBox, (windowHeight - tCardsHBoxHeight - pCardsHBoxHeight) / 2)
          //AnchorPane.setLeftAnchor(tCardsHBox, 0)
          //AnchorPane.setRightAnchor(tCardsHBox, 0)
          //AnchorPane.setBottomAnchor(tCardsHBox, (windowHeight - tCardsHBoxHeight - pCardsHBoxHeight) / 2 + pCardsHBoxHeight)

          val scoller = new ScrollPane()

          scoller.minWidth  = ScrollerWidth
          scoller.minHeight = scrollerHeight
          //scoller.alignment = Pos.Center

          AnchorPane.setTopAnchor(scoller, (windowHeight - scrollerHeight - pCardsHBoxHeight) / 2)
          AnchorPane.setLeftAnchor(scoller, 0)
          AnchorPane.setRightAnchor(scoller, 0)

          val playButton = new Button("PLAY")
          playButton.minWidth = buttonWidth
          playButton.minHeight = buttonHeight
          playButton.onMouseClicked = (event: MouseEvent) => {
            if gameOption.nonEmpty then
              val game = gameOption.get
              val cardUsedForTaking = game.players(game.turn).cards.filter(_.selected)
              val cardsToBeTaken = game.tableCards.filter(_.selected)
              val gameStates: (Boolean, Boolean, Boolean) = game.playTurn(cardUsedForTaking, cardsToBeTaken)
              if !gameStates._1 then
                new Alert(AlertType.Information, "Invalid move!").showAndWait()
                cardUsedForTaking.foreach(_.selectToggle)
                cardsToBeTaken.foreach(_.selectToggle)
              else if gameStates._2 && !gameStates._3 then
                new Alert(AlertType.Information, "New Round!").showAndWait()
                //tCardsHBox.children = List()
                //pCardsHBox.children = List()
              else if gameStates._3 then
                new Alert(AlertType.Information, "Game Over!").showAndWait()
              updateTableCards(game)
              updatePlayerCards(game)
              //cardsToBeTaken.clear()
              //cardUsedForTaking = None
              //midSelection = false
          }

          AnchorPane.setBottomAnchor(playButton, ((windowHeight - scrollerHeight - pCardsHBoxHeight) / 2 - buttonHeight) / 3 + pCardsHBoxHeight)
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

          val nameTilePane = new TilePane()
          nameTilePane.minWidth = buttonWidth

          var nameText = new Text("Start or load a game!")
          nameText.setFont(new Font("Comic Sans MS", 16))

          nameTilePane.alignment = Pos.BottomCenter
          nameTilePane.children = nameText

          AnchorPane.setBottomAnchor(nameTilePane, pCardsHBoxHeight )
          AnchorPane.setLeftAnchor(nameTilePane, (windowWidth / 2) - (buttonWidth / 2))
          AnchorPane.setRightAnchor(nameTilePane, (windowWidth / 2) - (buttonWidth / 2))


          val pCardsHBox = new HBox(5)
          pCardsHBox.minWidth  = pCardsHBoxWidth
          pCardsHBox.minHeight = pCardsHBoxHeight
          pCardsHBox.alignment = Pos.BottomCenter

          AnchorPane.setBottomAnchor(pCardsHBox, 0)
          AnchorPane.setLeftAnchor(pCardsHBox, 385)
          AnchorPane.setRightAnchor(pCardsHBox, 385)


          anchor.onKeyPressed = (e: KeyEvent) => {
            if gameOption.nonEmpty && e.code == KeyCode.S then
              val data = ObservableBuffer[Player]()
              gameOption.get.players.foreach(data += _)
              val table = new TableView(data)
              //table.minWidth = windowWidth * 0.5
              table.maxWidth = 5 * tableCardWidth
              //table.minHeight = windowHeight * 0.3
              table.maxHeight = 24 + 25 * gameOption.get.players.size //windowHeight * 0.3
              val col1 = new TableColumn[Player, Int]("Player #")
              col1.cellValueFactory = cdf => ObjectProperty(cdf.value.playerNumber)
              val col2 = new TableColumn[Player, String]("Name")
              col2.cellValueFactory = cdf => StringProperty(cdf.value.playerName)
              val col3 = new TableColumn[Player, Int]("Score")
              col3.cellValueFactory = cdf => ObjectProperty(gameOption.get.scores.find(_._1 == cdf.value).get._2)
              val col4 = new TableColumn[Player, Int]("Cards Collected")
              col4.cellValueFactory = cdf => ObjectProperty(cdf.value.returnPileSize)
              val col5 = new TableColumn[Player, Int]("Spades Collected")
              col5.cellValueFactory = cdf => ObjectProperty(cdf.value.returnSpadesSize)
              val col6 = new TableColumn[Player, Int]("Sweeps")
              col6.cellValueFactory = cdf => ObjectProperty(cdf.value.returnSweeps)
              val col7 = new TableColumn[Player, Int]("Aces")
              col7.cellValueFactory = cdf => ObjectProperty(cdf.value.returnAcesSize)
              table.columns ++= ObservableBuffer(col1, col2, col3, col4, col5, col6, col7)
              table.setColumnResizePolicy(TableView.ConstrainedResizePolicy)
              table.sortOrder.add(col1)
              stack.children += table
          }

          anchor.onKeyReleased = (e: KeyEvent) => {
            if gameOption.nonEmpty && e.code == KeyCode.S then
              stack.children = anchor
          }
          //anchor.children += menuBar
          //anchor.children += tCardsHBox
          //anchor.children += playButton
          //anchor.children += playerName
          //anchor.children += nameTilePane
          //anchor.children += pCardsHBox

          val gameView = Vector(menuBar, scoller, playButton, nameTilePane, pCardsHBox)

          scoller.content = tCardsHBox

          anchor.children = gameView
          stack.children = anchor

          root = stack

          def gameInProgress: Unit =
            if gameOption.nonEmpty then
              val game = gameOption.get
              updatePlayerCards(game)
              updateTableCards(game)

          def updatePlayerCards(game: Game) =
            pCardsHBox.children.clear()
            game.players(game.turn).cards.foreach(k => {
                val img = new Image(s"file:cards/${k.toString.toLowerCase.replace(' ', '_')}.png", playerCardWidth, playerCardHeight, true, true)
                pCardsHBox.children += new ImageView(img)
                })
              pCardsHBox.children.foreach(k => k.onMouseClicked = (event: MouseEvent) => replaceSelectedPlayerCard(pCardsHBox.children.indexOf(k), game))

          def updateTableCards(game: Game) =
            val t = new Text(s"${game.players(game.turn).playerName}")
            t.setFont(new Font("Comic Sans MS", 16))
            nameTilePane.children = t
            tCardsHBox.children.clear()
            game.tableCards.foreach(k => tCardsHBox.children += getImageView(k, 165, tableCardHeight * 0.95))
              tCardsHBox.children.foreach(k => k.onMouseClicked = (event: MouseEvent) => replaceSelectedTableCard(tCardsHBox.children.indexOf(k), game))

          def replaceSelectedPlayerCard(index: Int, game: Game): Unit =
            //if !midSelection then
            val card = game.players(game.turn).cards(index)
            val img  = new Image(s"file:cards/${game.players(game.turn % game.players.size).cards(index).toString.toLowerCase.replace(' ', '_')}.png", 150, playerCardHeight, true, true)
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
            val img  = new Image(s"file:cards/${game.tableCards(index).toString.toLowerCase.replace(' ', '_')}.png", tableCardWidth, tableCardHeight * 0.95, true, true)
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