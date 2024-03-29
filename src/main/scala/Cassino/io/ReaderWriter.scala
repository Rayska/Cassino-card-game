package Cassino.io

import Cassino.*
import java.io.*
import java.time.*
import java.util.NoSuchElementException
import scala.io.Source
import scala.collection.mutable.Buffer

val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

object ReaderWriter {

  var gameOption: Option[Game] = None

  //HERE STARTS SAVEFILE WRITING AND CREATION
  def writeSaveFile(gameActual: Game, fileName: String = "CassinoSavefile.txt") =
    try
      gameOption = Some(gameActual)
      val game = gameOption.get
      val w = new PrintWriter(new File(s"${fileName}"))
      w.write(s"CASS${game.vNumber}${java.time.LocalDate.now.toString.split('-').reverse.mkString("")}\n")  //Version Number and date
      w.write(createGMEBlock)                                                                               //GME block
      for player <- game.players do
        w.write(createPlRBlock(player))
      w.write(createDCKBlock)
      w.write("END00")
      w.close()
    catch
      case e:NoSuchElementException => throw NoSuchElementException(s"${e} is None so .get causes an error")//Should be unreachable but you never know

  private def createGMEBlock: String =
    val game = gameOption.get
    val lastToTake = if game.returnLastToTake == -1 then "-" else scoreFormattingCreate(game.returnLastToTake)
    val roundNumber: String = scoreFormattingCreate(game.returnRoundNumber)
    val gameOver: String = if game.returnGameOver then "T" else "F"
    val GME = Buffer[String]("GME", s"D${game.returnDealer}" ,s"T${game.returnTurn}", s"L${lastToTake}", s"R${roundNumber}", s"G${gameOver}")   //Create GME block
    game.returnTableCards.foreach(GME += cardFormattingCreate(_))                                                 //Table Cards
    GME.mkString("") + "\n"

  private def createPlRBlock(player: Player): String =
    val game = gameOption.get
    val score = scoreFormattingCreate(game.returnScore(player))
    val name = player.playerName + "."
    val sweeps = scoreFormattingCreate(player.sweeps)
    val PLR = Buffer[String]("PLR", player.playerNumber.toString, score, sweeps, name)
    player.cards.foreach(PLR += cardFormattingCreate(_))
    PLR += ","
    player.pile.foreach(PLR += cardFormattingCreate(_))
    player match
      case Human(number, name) => PLR += "U"
      case COM(number, name, game)  => PLR += "C"
    PLR.mkString("") + "\n"

  private def createDCKBlock: String =
    val game = gameOption.get
    val DCK = Buffer[String]("DCK")
    game.deck.deck.foreach(DCK += cardFormattingCreate(_))
    DCK.mkString("") + "\n"

  def scoreFormattingCreate(score: Int): String =
    if score < 10 then
      score.toString
    else
      ALPHABET(score.toString.last.toInt - 48).toString

  private def cardFormattingCreate(card: Card): String = //Creates a two character combination that is unique for each card
    var ret = ""
    card.suit match
      case 0  => ret += "C"
      case 1  => ret += "D"
      case 2  => ret += "H"
      case 3  => ret += "S"
      case _  => ret += "I" //Invalid suit
    card.tableValue match
      case 1  => ret += "A"
      case 11 => ret += "J"
      case 12 => ret += "Q"
      case 13 => ret += "K"
      case 10 => ret += "X" //10 of any suit
      case _  => ret += (card.tableValue).toString  //in order to squeeze all the cards into a sigle digit/character, all numbered cards (2 - 10) will be saved as 1 - 9
    ret

  //HERE STARTS LOADING A SAVEFILE AND CREATING A GAME FROM IT
  def loadSaveFile(fileName: String): Game =
    try
      val source = Source.fromFile(fileName)
      var dataString = source.mkString("")
      source.close()
      val blocks = dataString.split('\n').tail
      var GMEinfo = (0, 0, 0, 0, false, Buffer[Card]())
      val playersAndScores = Buffer[(Player, Int)]()
      var deck = new Deck
      var endOfFile = false
      if !dataString.contains("CASS") then throw CorruptedCassinoFileException(s"Unknown file type, does not contain header ${"CASS"}")
      else
        dataString = dataString.dropWhile(_ !='\n')
        for block <- blocks if !endOfFile do
            block.take(3).mkString("") match
              case "GME" => GMEinfo = readGMEBlock(block)
              case "PLR" => playersAndScores += readPLRBlock(block)
              case "DCK" => deck = readDCKBlock(block)
              case "END" => endOfFile = true
              case  _    =>
      val newGame = new Game(playersAndScores.map(_._1) , deck)
      val computers = playersAndScores.filter(k =>
        k._1 match
              case COM(number: Int, name: String, oldGame: Option[Game]) => true
              case _ => false
      )
      computers.foreach(_._1.changeGame(newGame))
      newGame.setDealer(GMEinfo._1)                                       //Set the dealer
      newGame.setTurn(GMEinfo._2 % (playersAndScores.map(_._1)).size)     //Set the turn
      GMEinfo._6.foreach(newGame.addCardToTable(_))                       //Add TableCards
      newGame.setLastTaker(GMEinfo._3)
      newGame.setRoundOver(GMEinfo._4)
      newGame.setGameOver(GMEinfo._5)
      playersAndScores.foreach(k => newGame.addPlayerScores(k._1, k._2))
      val lmao = newGame
      newGame
    catch
      case e: FileNotFoundException => throw CorruptedCassinoFileException(s"File with name: ${fileName} not found")
      case e:IndexOutOfBoundsException => throw CorruptedCassinoFileException("Player index out of bounds")

  private def readGMEBlock(block: String): (Int, Int, Int, Int, Boolean, Buffer[Card]) =
    val dealer = block(4).toInt - 48
    val turn = block(6).toInt - 48
    val lastToTake =  scoreFormattingRead(block(8))
    val roundNumber: Int = scoreFormattingRead(block(10))
    val gameOver: Boolean = if block(12) == 'T' then true else false
    val cards = Buffer[Card]()
    val cardsAsString = Buffer[String]()
    block.drop(13).sliding(2,2).foreach(cardsAsString += _.mkString(""))
    for cardString <- cardsAsString do
      cards += cardFormattingRead(cardString)
    (dealer, turn, lastToTake, roundNumber, gameOver, cards)

  private def readPLRBlock(block: String): (Player, Int) =
    val playerNumber = block(3).toInt - 48 //Char to Int conversion is not '1' to 1
    val playerScore = scoreFormattingRead(block(4))
    val playerSweeps = scoreFormattingRead(block(5))
    val playerName = block.drop(6).takeWhile(_ != '.').mkString("")
    val playerCards = Buffer[Card]()
    val cardsAsString = Buffer[String]()
    val playerPile = Buffer[Card]()
    val pileAsString= Buffer[String]()
    block.dropWhile(_ != '.').tail.dropRight(1).takeWhile(_ != ',').sliding(2,2).foreach(cardsAsString += _.mkString("")) //player's cards
    block.dropWhile(_ != ',').tail.dropRight(1).sliding(2,2).foreach(pileAsString += _.mkString(""))                      //player's pile
    for cardString <- cardsAsString do
      playerCards += cardFormattingRead(cardString)
    for pileString <- pileAsString do
      playerPile += cardFormattingRead(pileString)
    var player: Player = new Human(0, "lmao")
    if block.last == 'U' then
      player = new Human(playerNumber, playerName)
    else if block.last == 'C' then
      player = new COM(playerNumber, playerName)
    playerCards.foreach(k => player.addCardToPlayer(Some(k)))
    playerPile.foreach(player.addCardToPile(_))
    for i <- Range(0,playerSweeps) do player.addSweep()
    (player, playerScore)

  private def readDCKBlock(block: String): Deck =
    val cards = Buffer[Card]()
    val cardsAsString = Buffer[String]()
    block.drop(3).sliding(2,2).foreach(cardsAsString += _.mkString(""))
    cardsAsString.foreach(cards += cardFormattingRead(_))
    new Deck(cards)

  private def scoreFormattingRead(score: Char): Int = //Turns ALPHABETS back into integers
    if score == '-' then
      -1
    else if ALPHABET.contains(score) then
        ALPHABET.indexOf(score) + 10
    else
      score.toInt - 48

  private def cardFormattingRead(cardString: String): Card = //Creates a card based on a two character String
    if cardString.length == 2 then
      def valueFinder(char: Char): Int =
        if ALPHABET.contains(char) then
          char match
            case 'A' => 12
            case 'K' => 11
            case 'Q' => 10
            case 'J' => 9
            case 'X' => 8 //10 of any suit
            case _   => throw CorruptedCassinoFileException(s"${cardString(0)}${cardString(1)} is an unknown card with an invalid rank/value.")
        else
          char.toInt - 2 - 48
      cardString(0) match
            case 'C' => new Card(valueFinder(cardString(1)) + 0 * 13)
            case 'D' => new Card(valueFinder(cardString(1)) + 1 * 13)
            case 'H' => new Card(valueFinder(cardString(1)) + 2 * 13)
            case 'S' => new Card(valueFinder(cardString(1)) + 3 * 13)
            case _   => throw CorruptedCassinoFileException(s"${cardString(0)}${cardString(1)} is an unknown card with an invalid suit.")
    else
      throw CorruptedCassinoFileException(s"${cardString} length is not 2 and thus cannot be a valid card")

}
