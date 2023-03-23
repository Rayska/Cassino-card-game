package Cassino.io

import Cassino.*
import java.io.*
import scala.io.Source
import java.time.*
import scala.collection.mutable.Buffer


val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

object ReaderWriter {

  var game = new Game(Buffer[Player](new Human(-1, "Place Holder"))) //Place holder so that each method can access it without having it as a parameter

  //HERE STARTS SAVEFILE WRITING AND CREATION
  def writeSaveFile(gameActual: Game) =
    game = gameActual
    val w = new PrintWriter(new File("Demo.txt"))
    w.write(s"CASS${game.vNumber}${java.time.LocalDate.now.toString.split('-').reverse.mkString("")}\n")  //Version Number and date
    w.write(createGMEBlock)                                                                           //GME block
    for player <- game.players do
      w.write(createPlRBlock(player))
    w.write(createDCKBlock)
    w.write("END00")
    w.close()

  def createGMEBlock: String =
    val GME = Buffer[String]("GME", /*"",*/ s"D${game.dealer}" ,s"T${game.turn}")   //Create GME block
    game.tableCards.foreach(GME += cardFormattingCreate(_))                                                 //Table Cards
    //GME.update(1, GME.tail.mkString("").length.toString)
    GME.mkString("") + "\n"

  def createPlRBlock(player: Player): String =
    val score = game.returnScore(player)
    val name = player.playerName + "."
    val scoreFormatted: String =
      if score < 10 then
        score.toString
      else
        ALPHABET(score.toString.last.toInt - 48).toString
    val PLR = Buffer[String]("PLR", /*"",*/ player.playerNumber.toString, scoreFormatted, name)
    player.cards.foreach(PLR += cardFormattingCreate(_))
    PLR += ","
    player.pile.foreach(PLR += cardFormattingCreate(_))
    player match
      case Human(number, name)      => PLR += "U"
      case COM(number, name, game)  => PLR += "C"
    //PLR.update(1, PLR.tail.mkString("").length.toString)
    PLR.mkString("") + "\n"

  def createDCKBlock: String =
    val DCK = Buffer[String]("DCK"/*, (game.deck.deck.size * 2).toString*/)
    game.deck.deck.foreach(DCK += cardFormattingCreate(_))
    DCK.mkString("") + "\n"

  def cardFormattingCreate(card: Card): String =
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
    val source = Source.fromFile(fileName)
    var dataString = source.mkString("")
    source.close()
    val blocks = dataString.split('\n').tail
    var GMEinfo = (0, 0, Buffer[Card]())
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
    val computers = playersAndScores.filter(k => k._1 match
                                                    case COM(number: Int, name: String, oldGame: Game) => true
                                                    case _ => false
    )
    computers.foreach(_._1.changeGame(newGame))
    newGame.setDealer(GMEinfo._1)                                       //Set the dealer
    newGame.setTurn(GMEinfo._2)                                         //Set the turn
    GMEinfo._3.foreach(newGame.addCardToTable(_))                       //Add TableCards
    try
      playersAndScores.foreach(k => newGame.addPlayerScores(k._1, k._2))
    catch
      case e:IndexOutOfBoundsException => throw CorruptedCassinoFileException("Player index out of bounds")
    newGame

  def readGMEBlock(block: String): (Int, Int, Buffer[Card]) =
    //val blocksize = block.slice(3,5)
    val dealer = block(4).toInt - 48
    val turn = block(6).toInt - 48
    val cards = Buffer[Card]()
    val cardsAsString = Buffer[String]()
    block.drop(7).sliding(2,2).foreach(cardsAsString += _.mkString(""))
    for cardString <- cardsAsString do
      cards += cardFormattingRead(cardString)
    (dealer, turn, cards)

  def readPLRBlock(block: String): (Player, Int) =
    //val blocksize = block.slice(3,5)
    val playerNumber = block(3).toInt - 48 //Char to Int conversion is not '1' to 1
    val playerScore =
      if ALPHABET.contains(block(4)) then
        ALPHABET.indexOf(block(4)) + 10
      else
        block(4).toInt - 48
    val playerName = block.drop(5).takeWhile(_ != '.').mkString("")
    val playerCards = Buffer[Card]()
    val cardsAsString = Buffer[String]()
    val playerPile = Buffer[Card]()
    val pileAsString= Buffer[String]()
    block.dropWhile(_ != '.').tail.dropRight(1).takeWhile(_ != ',').sliding(2,2).foreach(cardsAsString += _.mkString("")) //player's cards
    for cardString <- cardsAsString do
      playerCards += cardFormattingRead(cardString)
    block.dropWhile(_ != ',').tail.dropRight(1).sliding(2,2).foreach(pileAsString += _.mkString(""))                      //player's pile
    for pileString <- pileAsString do
      playerPile += cardFormattingRead(pileString)
    var player: Player = new Human(0, "lmao")
    if block.last == 'U' then
      player = new Human(playerNumber, playerName)
    else if block.last == 'C' then
      player = new COM(playerNumber, playerName)
    playerCards.foreach(player.addCardToPlayer(_))
    playerPile.foreach(player.addCardToPile(_))
    (player, playerScore)

  def readDCKBlock(block: String): Deck =
    val cards = Buffer[Card]()
    val cardsAsString = Buffer[String]()
    block.drop(3).sliding(2,2).foreach(cardsAsString += _.mkString(""))
    cardsAsString.foreach(cards += cardFormattingRead(_))
    new Deck(cards)

  def cardFormattingRead(cardString: String): Card =
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
