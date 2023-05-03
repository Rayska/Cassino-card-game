package Cassino.GUI

import Cassino.*
import Cassino.io.{CorruptedCassinoFileException, ReaderWriter}

import scala.collection.mutable.Buffer

import scalafx.Includes.*
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.*
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalafx.beans.binding.ObjectBinding
import scalafx.event.ActionEvent
import scalafx.geometry.*
import scalafx.scene.Scene
import scalafx.scene.layout.*
import scalafx.scene.control.*
import scalafx.scene.control.Alert.*
import scalafx.scene.control.ScrollPane.*
import scalafx.scene.control.TableView.ResizeFeatures
import scalafx.scene.image.*
import scalafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import scalafx.scene.text.*
import scalafx.scene.paint.{Color, Paint}
import scalafx.collections.ObservableBuffer

import java.util.NoSuchElementException

object Main extends JFXApp3 {

  private var gameOption: Option[Game] = None
  private val windowWidth = 1920
  private val windowHeight = 1000
  private val pCardsHBoxWidth = windowWidth * 0.321
  private val pCardsHBoxHeight = windowHeight * 0.23
  private val tCardsScrollerWidth = windowWidth
  private val tCardsScrollerHeight = windowHeight * 0.275
  private val buttonWidth = windowWidth * 0.117
  private val buttonHeight = windowHeight * 0.075
  private val tableCardWidth = windowWidth * 0.086
  private val tableCardHeight = tCardsScrollerHeight
  private val playerCardWidth = 150
  private val playerCardHeight = pCardsHBoxHeight
  private val opponentCardHeight = 35
  private val opponentCardWidth = playerCardWidth / 5
  private val playLogCardHeight = 80
  private val playLogCardWidth = playerCardWidth / 2
  private val playLogScrollerWidth = (windowWidth - pCardsHBoxWidth) / 2 * 0.95
  private val playLogScrollerHeight = windowHeight / 2 * 0.95

  private var gameInProgess = false
  private var playLogSize = 0

  def start(): Unit =
    val mainMenu = new PrimaryStage {
        title = "Cassino"
        scene = new Scene(windowWidth, windowHeight) {
          val stack = new StackPane() //So that scoreboard can be displayed on top of everything else

          val anchor = new AnchorPane //Base for everything but scoreboard

          val menuBar  = new MenuBar
          val fileMenu = new Menu("Game File")
          val newFile  = new MenuItem("New")
          val loadFile = new MenuItem("Load")
          val saveFile = new MenuItem("Save")

          fileMenu.items = List(newFile, loadFile, saveFile)

          menuBar.menus = List(fileMenu)
          menuBar.minWidth = windowWidth

          newFile.onAction = (event: ActionEvent) => newGame()

          loadFile.onAction = (event: ActionEvent) => { //Load a previously saved game
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
              stack.children = anchor
              opponentHBox.children = Vector.tabulate(gameOption.get.players.size - 1)(k => new VBox()) //opponents cards in the top of screen
              gameInProgess = true
              playLogVBox.children.clear()
              playLogSize = 0
              updateAllCards()
              gameOption.get.turnCheck()
          }

          saveFile.onAction = (event: ActionEvent) => { //Save the on going game into a .txt file
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
                  gameOption.get.players(gameOption.get.returnTurn) match
                    case COM(number: Int, name: String, oldGame: Option[Game]) => new Alert(AlertType.Information, "Game can only be saved on a User's turn!").showAndWait()
                    case _ => ReaderWriter.writeSaveFile(gameOption.get, fileName)
                catch
                  case e:NoSuchElementException => new Alert(AlertType.Information, "No game to save!").showAndWait()
              }
            }
          }

          val opponentHBox = new HBox(30) //Opponents cards
          opponentHBox.minWidth = windowWidth
          opponentHBox.alignment = Pos.BottomCenter

          AnchorPane.setTopAnchor(opponentHBox, 25)

          val deckSizePane = new TilePane() //Shows number of remaining cards in deck
          deckSizePane.minWidth = buttonWidth

          var deckSize = new Text("")
          deckSize.setFont(new Font("Comic Sans MS", 16))

          deckSizePane.alignment = Pos.BottomCenter
          deckSizePane.children = deckSize

          AnchorPane.setTopAnchor(deckSizePane, pCardsHBoxHeight )
          AnchorPane.setLeftAnchor(deckSizePane, (windowWidth / 2) - (buttonWidth / 2))
          AnchorPane.setRightAnchor(deckSizePane, (windowWidth / 2) - (buttonWidth / 2))

          val tCardsHBox = new HBox(5)                    // Cards on the table will be displayed here
          tCardsHBox.minWidth  = tCardsScrollerWidth - 2
          tCardsHBox.minHeight = tCardsScrollerHeight - 2 //To keep scrollbars from appearing before cards overflow the windowWidth
          tCardsHBox.alignment = Pos.Center

          val tCardsScroller = new ScrollPane()           //In the unfortunate case that the cards on the table cant fit on the screen

          tCardsScroller.minWidth  = tCardsScrollerWidth
          tCardsScroller.minHeight = tCardsScrollerHeight

          AnchorPane.setTopAnchor(tCardsScroller, (windowHeight - tCardsScrollerHeight - pCardsHBoxHeight) / 2)
          AnchorPane.setLeftAnchor(tCardsScroller, 0)
          AnchorPane.setRightAnchor(tCardsScroller, 0)

          val playButton = new Button("PLAY")                             //Button to play selected cards and create a new game when there is no game in progress
          playButton.minWidth = buttonWidth
          playButton.minHeight = buttonHeight
          playButton.onMouseClicked = (event: MouseEvent) => playAction()
          AnchorPane.setBottomAnchor(playButton, ((windowHeight - tCardsScrollerHeight - pCardsHBoxHeight) / 2 - buttonHeight) / 3 + pCardsHBoxHeight)
          AnchorPane.setLeftAnchor(playButton, windowWidth / 2 - buttonWidth / 2)
          AnchorPane.setRightAnchor(playButton, windowWidth / 2 - buttonWidth / 2)

          val nameTilePane = new TilePane()                               // Name of the player whose turn it is
          nameTilePane.minWidth = buttonWidth

          var nameText = new Text("Start or load a game!")
          nameText.setFont(new Font("Comic Sans MS", 16))

          nameTilePane.alignment = Pos.BottomCenter
          nameTilePane.children = nameText

          AnchorPane.setBottomAnchor(nameTilePane, pCardsHBoxHeight )
          AnchorPane.setLeftAnchor(nameTilePane, (windowWidth / 2) - (buttonWidth / 2))
          AnchorPane.setRightAnchor(nameTilePane, (windowWidth / 2) - (buttonWidth / 2))

          val pCardsHBox = new HBox(5) //Cards of the player whose turn it is
          pCardsHBox.minWidth  = pCardsHBoxWidth
          pCardsHBox.minHeight = pCardsHBoxHeight
          pCardsHBox.alignment = Pos.BottomCenter

          AnchorPane.setBottomAnchor(pCardsHBox, 0)
          AnchorPane.setLeftAnchor(pCardsHBox, 385)
          AnchorPane.setRightAnchor(pCardsHBox, 385)

          val playLogVBox = new VBox(5) //Play log

          val playLogScroller = new ScrollPane() //Play log

          playLogScroller.minWidth  = playLogScrollerWidth
          playLogScroller.minHeight = playLogScrollerHeight
          playLogScroller.maxHeight = playLogScrollerHeight

          playLogScroller.setFitToWidth(true)
          playLogScroller.setFitToHeight(true)

          AnchorPane.setBottomAnchor(playLogScroller, 0)
          AnchorPane.setRightAnchor(playLogScroller, 0)

          val asdf = Buffer(KeyCode.A, KeyCode.S, KeyCode.D, KeyCode.F) //keys to pick own cards
          val digits = Buffer(KeyCode.Digit1, KeyCode.Digit2, KeyCode.Digit3, KeyCode.Digit4, KeyCode.Digit5, KeyCode.Digit6, KeyCode.Digit7, KeyCode.Digit8, KeyCode.Digit9, KeyCode.Digit0) // keys to pick cards from the table
          anchor.onKeyPressed = (e: KeyEvent) => {
            val ec = e.code
            if gameOption.nonEmpty then
              val game = gameOption.get
              if e.code == KeyCode.G then // show scoreboard
                if ec == KeyCode.G then
                  stack.children += scoreboard(gameOption.get)
              else if asdf.contains(ec) then
                val index = asdf.indexOf(ec)
                if game.players(game.returnTurn).cards.size > index then
                  replaceSelectedPlayerCard(index,game)
              else if digits.contains(ec) then
                val index = digits.indexOf(ec)
                if game.returnTableCards.size > index then
                  replaceSelectedTableCard(index, game)
          }

          anchor.onKeyReleased = (e: KeyEvent) => {
            if gameOption.nonEmpty then
              if e.code == KeyCode.Enter || e.code == KeyCode.X then //Enter could only be detected on release
                playAction()
              else if e.code == KeyCode.G then //Hide the scoreboard
                stack.children = anchor
          }

          anchor.setBackground(Background.fill(Color.color(0.208, 0.47, 0.275))) //#357846 (0x)
          tCardsHBox.setBackground(Background.fill(Color.color(0.208, 0.47, 0.275)))
          tCardsScroller.setBackground(Background.fill(Color.color(0.208, 0.47, 0.275)))
          playLogVBox.setBackground(Background.fill(Color.color(0.208, 0.47, 0.275)))
          playLogScroller.setBackground(Background.fill(Color.color(0.208, 0.47, 0.275)))

          val gameView = Vector(menuBar, opponentHBox, deckSizePane, tCardsScroller, playButton, nameTilePane, pCardsHBox, playLogScroller) //default view, no scoreboard

          tCardsScroller.content = tCardsHBox
          playLogScroller.content = playLogVBox

          anchor.children = gameView
          stack.children = anchor

          root = stack

          def playAction() : Unit =  //This is called when user wants to play the picked cards
            if gameInProgess then
              val game = gameOption.get
              val cardUsedForTaking = game.players(game.returnTurn).cards.filter(_.returnSelected())
              val cardsToBeTaken = game.returnTableCards.filter(_.returnSelected())
              var roundNumber = game.returnRoundNumber
              val successfulMoveByHuman = game.playTurn(cardUsedForTaking, cardsToBeTaken)
              if !successfulMoveByHuman then //Invalid move by User
                new Alert(AlertType.Information, "Invalid move!").showAndWait()
                cardUsedForTaking.foreach(_.unselect())
                cardsToBeTaken.foreach(_.unselect())
                updateAllCards()
              else if roundNumber != game.returnRoundNumber && !game.returnGameOver then //Round over but game continues
                new Alert(AlertType.Information, ""){headerText = "New Round!"}.showAndWait()
                updateAllCards()
              else if !game.returnGameOver then //Successful move by User, round didn't end, game didn't end
                while game.turnCheck() do
                  if roundNumber != game.returnRoundNumber && !game.returnGameOver then
                    new Alert(AlertType.Information, ""){headerText = "New Round!"}.showAndWait()
                    updateAllCards()
                  else if game.returnGameOver then
                    gameOver(game)
                updateAllCards()
              else //Successful move by user, game ended
                gameOver(game)
                updateAllCards()
            else //No game in progress, so create a new one
              newGame()

          def updateAllCards(): Unit = //Called whenever turn is switched and player's cards, table cards, player's name, deck size and opponents cards need to be updated
            if gameOption.nonEmpty then
              val game = gameOption.get
              if game.returnPlayLog.size != playLogSize then
                game.returnPlayLog.takeRight(game.returnPlayLog.size - playLogSize).foreach(playLogVBox.children += getPlayLogCell(_))
                val lmao = playLogVBox.heightProperty().toInt
                if playLogSize != 0 then
                  playLogScroller.vvalueProperty().bind(playLogVBox.heightProperty())
                playLogSize = game.returnPlayLog.size
              updatePlayerCards(game)
              updateTableCards(game)
              updateOpponentCards(game)
              if gameInProgess then
                val name = new Text(s"${game.players(game.returnTurn).playerName}")
                val deckSize = new Text(s"Cards left in deck: ${game.deck.returnDeckSize}")
                val deckActualSize = game.deck.returnDeckSize
                name.setFont(new Font("Comic Sans MS", 16))
                deckSize.setFont(new Font("Comic Sans MS", 16))
                deckSizePane.children = deckSize
                nameTilePane.children = name

          def updatePlayerCards(game: Game) =
            pCardsHBox.children.clear()
            game.players(game.returnTurn).cards.foreach(k => {
                val img = new Image(s"file:cards/${k.toString.toLowerCase.replace(' ', '_')}.png", playerCardWidth, playerCardHeight, true, true)
                pCardsHBox.children += new ImageView(img)
                })
            pCardsHBox.children.foreach(k => k.onMouseClicked = (event: MouseEvent) => replaceSelectedPlayerCard(pCardsHBox.children.indexOf(k), game))

          def updateTableCards(game: Game) =
            tCardsHBox.children.clear()
            game.returnTableCards.foreach(k => tCardsHBox.children += getImageView(k, 165, tableCardHeight * 0.95))
              tCardsHBox.children.foreach(k => k.onMouseClicked = (event: MouseEvent) => replaceSelectedTableCard(tCardsHBox.children.indexOf(k), game))

          def updateOpponentCards(game: Game): Unit =
            opponentHBox.children.clear()
            val players = game.players
            var i = (game.returnTurn + 1) % players.size
            var vBoxNum = 0
            var l = 0
            while i % players.size != game.returnTurn do
              val vBox = new VBox()
              val hBox = new HBox(3)
              hBox.minHeight(opponentCardHeight)
              hBox.minWidth(opponentCardWidth * 4 + 9)
              vBox.alignment = Pos.Center
              hBox.alignment = Pos.BottomCenter
              players(i % players.size).cards.foreach(k => hBox.children += getFlippedImageView(k, opponentCardHeight, playerCardWidth, true, true))
              val name = new Text(players(i % players.size).playerName)
              name.setFont(new Font("Comic Sans MS", 14))
              vBox.children = List(new Text(players(i % players.size).playerName), hBox)
              l += 1
              opponentHBox.children += vBox
              vBoxNum += 1
              i += 1

          def replaceSelectedPlayerCard(index: Int, game: Game): Unit =
            val card = game.players(game.returnTurn).cards(index)
            val img  = new Image(s"file:cards/${game.players(game.returnTurn % game.players.size).cards(index).toString.toLowerCase.replace(' ', '_')}.png", 150, playerCardHeight, true, true)
            var writableImg = new WritableImage(img.pixelReader.get, img.width.value.toInt, img.height.value.toInt)
            val pWriter = writableImg.pixelWriter
            if card.returnSelected() then
                card.unselect()
            else if game.players(game.returnTurn).cards.forall(!_.returnSelected()) then
              for i <- 0 until img.width.value.toInt; j <- 0 until img.height.value.toInt do
                pWriter.setArgb(i, j,img.pixelReader.get.getArgb(i,j) -0xA0000000)
              card.select()
            val newImageView = new ImageView(writableImg)
            newImageView.onMouseClicked = (event: MouseEvent) => {
                replaceSelectedPlayerCard(index, game)}
            pCardsHBox.children.update(index, newImageView)

          def replaceSelectedTableCard(index: Int, game: Game): Unit =
            val card = game.returnTableCards(index)
            val img  = new Image(s"file:cards/${game.returnTableCards(index).toString.toLowerCase.replace(' ', '_')}.png", tableCardWidth, tableCardHeight * 0.95, true, true)
            var writableImg = new WritableImage(img.pixelReader.get, img.width.value.toInt, img.height.value.toInt)
            val pWriter = writableImg.pixelWriter
            val newImageView = new ImageView(writableImg)
            if !card.returnSelected() then
              for i <- 0 until img.width.value.toInt; j <- 0 until img.height.value.toInt do
                pWriter.setArgb(i, j,img.pixelReader.get.getArgb(i,j) -0xA0000000)
              card.select()
            else
              card.unselect()
            newImageView.onMouseClicked = (event: MouseEvent) => {
                replaceSelectedTableCard(index, game)}
            tCardsHBox.children.update(index, newImageView)

          def gameOver(game: Game): Unit =
            stack.children += scoreboard(game)
            gameInProgess = false
            tCardsHBox.children.clear()
            pCardsHBox.children.clear()
            val mostPoints = game.returnScores.filter(_._2 == game.returnScores.maxBy(_._2)._2)
            var nameString = ""
            mostPoints.size match
              case 1 => nameString = s"${mostPoints.head._1.playerName} wins the game with ${mostPoints.head._2} points!"
              case 2 => nameString = s"${mostPoints.map(_._1.playerName).head} and ${mostPoints.map(_._1.playerName).last} tie the game with most (${mostPoints.head._2}) points!"
              case _ => val firstNames = mostPoints.map(_._1.playerName)
                        val lastName = firstNames.remove(firstNames.size - 1)
                        firstNames += "and"
                        nameString = s"${firstNames.mkString(", ")} ${lastName} tie the game with most (${mostPoints.head._2}) points!"
            val name = new Text(nameString)
            name.setFont(new Font("Comic Sans MS", 16))
            deckSizePane.children.clear()
            nameTilePane.children = name
            new Alert(AlertType.Information, nameString){headerText = "Game Over!"}.showAndWait()

          def newGame(): Unit = { //Create a new game
            val players = Buffer[Player]()
            val cDialog = new ChoiceDialog[Int](2, Seq(2,3,4,5,6)) { //Can add more players here if needed but other players cards might show up improperly on screen
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
                if i == numOfPlayers then                                                         //if i > numOfPlayers, dialog has been cancelled or otherwise closed
                  val game = new Game(players)
                  gameOption = Some(game)
                  tCardsHBox.children.clear()
                  pCardsHBox.children.clear()
                  game.players.foreach(_.changeGame(game))
                  stack.children = anchor
                  opponentHBox.children = Vector.tabulate(game.players.size - 1)(k => new VBox()) //opponents cards in the top of screen
                  gameInProgess = true
                  playLogVBox.children.clear()
                  playLogSize = 0
                  game.setupRound()
                  updateAllCards()
                  if game.returnGameOver then
                    gameOver(game)
                    updateAllCards()
              }
            }
          }

        def scoreboard(game: Game): TableView[Player] =                                           // return the Scoreboard to be shown
            val data = ObservableBuffer[Player]()
            game.players.foreach(data += _)
            val table = new TableView[Player](data)
            table.maxWidth = 5 * tableCardWidth
            table.maxHeight = 24 + 25 * gameOption.get.players.size
            val col1 = new TableColumn[Player, Int]("Player #")
            col1.cellValueFactory = cdf => ObjectProperty(cdf.value.playerNumber)
            val col2 = new TableColumn[Player, String]("Name")
            col2.cellValueFactory = cdf => StringProperty(cdf.value.playerName)
            val col3 = new TableColumn[Player, Int]("Score")
            col3.cellValueFactory = cdf => ObjectProperty(gameOption.get.returnScores.find(_._1 == cdf.value).get._2)
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
            table

          def getImageView(card: Card, width: Double, height: Double, preserveRatio: Boolean = true, smooth: Boolean = true): ImageView = //fetches the image of the given card
            new ImageView(new Image(s"file:cards/${card.toString.toLowerCase.replace(' ', '_')}.png", width, height, preserveRatio, smooth))

          def getFlippedImageView(card: Card, width: Double, height: Double, preserveRatio: Boolean = true, smooth: Boolean = true): ImageView = //Fetches the FlippedCard.png
            new ImageView(new Image(s"file:cards/FlippedCard.png", width, height, preserveRatio, smooth))

          def getPlayLogCell(move: (Player, Card, Buffer[Card])): VBox = //return a single cell for play log
            val vBox = new VBox()
            val hBox = new HBox(15)
            val hBoxPlay = new HBox(15)
            val hBoxTake = new HBox(15)
            vBox.alignment = Pos.CenterLeft
            hBox.alignment = Pos.Center
            hBoxPlay.alignment = Pos.CenterLeft
            hBoxTake.alignment = Pos.CenterLeft
            hBoxPlay.minWidth = playLogScrollerWidth / 4
            hBoxTake.minWidth = playLogScrollerWidth / 4 * 3
            val name = new Text(move._1.playerName)
            name.setFont(new Font("Comic Sans MS", 16))
            val played = new Text("Played:")
            played.setFont(new Font("Comic Sans MS", 12))
            val took = new Text("Took:")
            took.setFont(new Font("Comic Sans MS", 12))
            val cardPlayed = getImageView(move._2, playLogCardWidth, playLogCardHeight)
            vBox.children = List(name, hBox)
            hBox.children = List(hBoxPlay, hBoxTake)
            hBoxPlay.children = List(played, cardPlayed)
            hBoxTake.children = Buffer(took)
            move._3.foreach(hBoxTake.children += getImageView(_, playLogCardWidth, playLogCardHeight))
            vBox.setBackground(Background.fill(Color.color(0.208, 0.47, 0.275)))
            hBoxPlay.setBackground(Background.fill(Color.color(0.208, 0.47, 0.275)))
            vBox
        }
    }
}