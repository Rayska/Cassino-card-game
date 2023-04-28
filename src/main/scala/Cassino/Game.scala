package Cassino

import Cassino.io.ReaderWriter
import com.sun.prism.impl.Disposer.Target

import scala.collection.mutable.Buffer


class Game(var players: Buffer[Player], var deck: Deck = new Deck){

  val vNumber:      String                       = "0001"
  val scores:       Buffer[(Player, Int)]        = players.zip(Buffer.fill(players.size)(0))
  val tableCards:   Buffer[Card]                 = Buffer()
  var dealer:       Int                          = 0
  var turn:         Int                          = (dealer + 1) % players.size // Round starts from the player next to the dealer
  var lastToTake:   Int                          = -1                          // Player nummber of the last players who took cards from the table
  var roundNumber:  Int                          = 0
  var gameOver:     Boolean                      = false
  //val movesByCOM:   Buffer[(Card, Buffer[Card])] = Buffer()                    //Moves made by COM opponents since last Human player's moves, maybe add COM as an element too?

  /*def playGame(): Unit =
    while !gameOver do
      //this.playRound
      this.updateScores()
      dealer += 1
      turn = (dealer + 1) % players.size
      gameOver = !scores.forall(_._2 < 16)
      //endCondition = true//TEMP
    println(s"Game has ended with ${scores.maxBy(_._2)._1} as the Winner!")*/

  /*def playRound: Unit =
    var endConditionRound = false
    players.foreach(_.clearHand)                                                                                        // Make sure no players have cards before they have been dealt
    this.dealCards
    while !endConditionRound do
      this.playTurn
      endConditionRound = players.forall(_.cards.isEmpty)
      //endConditionRound = true//TEMP
    // NOTE: Not sure if in case of ties, extra points should be awarded to multiple players? Currently they are
    val mostCards  = players.filter(k => k.returnPileSize == players.maxBy(l => l.returnPileSize).returnPileSize)       // Players with largest piles
    val mostSpades = players.filter(k => k.returnSpadesSize == players.maxBy(l => l.returnSpadesSize).returnSpadesSize) // Players with most Spades
    mostCards.foreach(_.addPoints(1))                                                                                   // Add points for most cards
    mostSpades.foreach(_.addPoints(2))                                                                                  // Add points for most Spades
  */

  def setupRound(): Unit =
    this.deck = new Deck()
    tableCards.clear()
    players.foreach(k =>
      k.clearHand()
      k.clearPile()
      k.clearSweeps())
    this.dealCards()
    turnCheck()
    roundNumber += 1

  def endRound(): Unit =                                                                                             //Return value states if game is over
    val multipleScoreReceiversAllowed = false                                                                           //Can be decided later whether everyone with most cards/spades gets the points.
    tableCards.foreach(players.find(_.playerNumber == lastToTake).get.addCardToPile(_))
    //val mostCards  = players.filter(k => k.returnPileSize == players.maxBy(l => l.returnPileSize).returnPileSize)       // Players with largest piles
    //val mostSpades = players.filter(k => k.returnSpadesSize == players.maxBy(l => l.returnSpadesSize).returnSpadesSize) // Players with most Spades
    val mostCards = players.groupBy(_.returnPileSize)(players.groupBy(_.returnPileSize).keys.max)
    val mostSpades = players.groupBy(_.returnSpadesSize)(players.groupBy(_.returnSpadesSize).keys.max)
    if multipleScoreReceiversAllowed then
      mostCards.foreach(_.addPoints(1))                                                                                   // Add points for most cards
      mostSpades.foreach(_.addPoints(2))                                                                                  // Add points for most Spades
    else
      if mostCards.size == 1 then mostCards.head.addPoints(1)
      if mostSpades.size == 1 then mostSpades.head.addPoints(2)
    //this.deck.clear()
    this.updateScores()
    gameOver = scores.exists(_._2 >= 16)
    dealer = (dealer + 1) % players.size
    turn = (dealer + 1) % players.size
    lastToTake = -1
    //if gameOver then
    //  return true
    //false

  /*def playTurn: (Buffer[(Card, Buffer[Card])], Buffer[Card]) = //Separate into two or more methods where one part is dedicated to handling input (moves)
    var whosTurn = players(turn % players.size)                                                                         // Which player's turn it is (indexes so starts from 0)
    val take = Buffer[(Card, Buffer[Card])]((new Card(52), Buffer(new Card(52))))                                       // Temporary until implementation for input exists
    val place = Buffer[Card](new Card(52))                                                                              // Temporary until implementation for input exists
    var moveIsValid: Boolean = false
    if take.nonEmpty then                                                                                               // Taking cards from the table this turn
      while !moveIsValid do
        moveIsValid = true                                                                                              // Temporary until implementation for input exists
    else if place.nonEmpty then                                                                                         // Placing cards on the table this turn
      while !moveIsValid do
        moveIsValid = true                                                                                              // Temporary until implementation for input exists
    else
      // No move made
    if whosTurn.cards.size < 4 then                                                                                     // Draws a card into the player's hand if it has less than 4 cards and deck is not empty
      deck.draw match
        case Some(card) => whosTurn.addCardToPlayer(card)
        case _          =>
    turn += 1
    (Buffer((new Card(52), Buffer(new Card(52), new Card(52)))), Buffer(new Card(52)))  */

  def playTurn(usedForTaking: Buffer[Card], toBeTaken: Buffer[Card]) : Boolean =
    var whosTurn = players(turn % players.size)
    if usedForTaking.nonEmpty then //&& usedForTaking.forall(usedForTaking.head == _) then
      if toBeTaken.nonEmpty then
        if checkMoveLegitimacyTake(usedForTaking, toBeTaken) then
          whosTurn.addCardToPile(usedForTaking.head)
          whosTurn.removeCardFromPlayer(usedForTaking.head)
          usedForTaking.head.unselect()
          toBeTaken.foreach(k =>
            k.unselect()
            whosTurn.addCardToPile(k)
            this.removeCardFromTable(k))
          whosTurn.addCardToPlayer(deck.draw)
          if tableCards.isEmpty then
            whosTurn.addSweep()
          lastToTake = whosTurn.playerNumber
          turn = (turn + 1) % players.size
        else
          return false
      else if checkPlayerHasCard(usedForTaking) then
        this.addCardToTable(usedForTaking.head)
        whosTurn.removeCardFromPlayer(usedForTaking.head)
        usedForTaking.head.unselect()
        whosTurn.addCardToPlayer(deck.draw)
        turn = (turn + 1) % players.size
      if players.forall(_.cards.isEmpty) then
        endRound()
        if !gameOver then
          setupRound()
        //turnCheck()
        return true
      else
        //turnCheck()
        return true
    false

  def turnCheck(): Boolean =
    players(turn) match
      case COM(number: Int, name: String, game: Option[Game]) =>
        players(turn).makePlay()
        true
      case _ =>
        println(s"game.turnCheck called for ${players(turn)}")
        false

  /*var whosTurn = players(turn % players.size)
    if move.collect(_._2).flatten.isEmpty && move.collect(_._1).nonEmpty && checkPlayerHasCard(move.collect(_._1)) then
      tableCards += move.head._1
      whosTurn.cards -= move.head._1
      move.head._1.selectToggle
      whosTurn.addCardToPlayer(deck.draw)
      turn = (turn + 1) % players.size
      return true
    else if checkMoveLegitimacyTake(move) && move.collect(_._1).nonEmpty then
      move.collect(_._2).flatten.foreach(k => {
        k.selectToggle
        whosTurn.addCardToPile(k)
        tableCards -= k
      })
      //what else happens when cards are taken?
      turn = (turn + 1) % players.size
      return true
    move.head._1.selectToggle
    move.collect(_._2).flatten.foreach(_.selectToggle)
    return false*/


//move: Buffer[(Card, Buffer[Card])]
  def checkMoveLegitimacyTake(usedForTaking: Buffer[Card], toBeTaken: Buffer[Card]): Boolean =
    //For each taking move the player has to determine which card is used to pick which cards
    val noDuplicates  = toBeTaken.map(_.cardID) == toBeTaken.map(_.cardID).distinct                                                               // There are no duplicates in the cards we are trying to take (same card cannot be taken multiple times)
    val playerHasCard = checkPlayerHasCard(usedForTaking)                                                             // Player has every(/the) card in their hand they are trying to use
    val tableHasCard  = toBeTaken.forall(k => tableCards.exists(_.cardID == k.cardID))                                // Cards we are trying to take are on the Table
    val oneAndSame    = onlyOneDistinct(usedForTaking)                                                                // Same card is used for every "taking" move
    if !(noDuplicates && playerHasCard && tableHasCard && oneAndSame) then
      return false
    SumCheck(usedForTaking.head, toBeTaken)


  def SumCheck(usedForTaking: Card, toBeTaken: Buffer[Card]): Boolean =
    val handValue = usedForTaking.handValue
    val tableValues = toBeTaken.map(_.tableValue)
    if !(tableValues.sum % handValue == 0) then
      return false
    val numOfPartitions = tableValues.sum / handValue
    recursive(0, tableValues, Buffer.fill(numOfPartitions)(0))

  def recursive(index: Int, tableValues: Buffer[Int], temp: Buffer[Int]): Boolean =
    if (index == tableValues.size) then
      return checkOutOfBounds(temp)
    var bool = false
    var ret = false
    for i <- Range(0,temp.size) do
      temp.update(i, temp(i) + tableValues(index))
      bool = recursive(index + 1, tableValues, temp)
      if bool then
        ret = true
      temp.update(i, temp(i) - tableValues(index))
    ret

  def checkOutOfBounds(v: Buffer[Int]): Boolean = v.tail.forall(_ == v.head)

  def checkPlayerHasCard(move: Buffer[Card]): Boolean =
    var truth = true
    var index = 0
    while truth && index < move.size do
      if !players(turn % players.size).cards.exists(_.cardID == move(index).cardID) then
        truth = false
      index += 1
    truth

  def onlyOneDistinct(usedForTaking: Buffer[Card]) = usedForTaking.forall(usedForTaking.head.cardID == _.cardID)

  def dealCards(): Unit =
    for player <- players do //to the players
      val hand = Buffer.tabulate(4)(k => deck.draw.getOrElse(new Card(52)))   // Card(52).toString will be 2 of Unknown suit
      for card <- hand do
        player.addCardToPlayer(Some(card))
    for card <- Buffer.tabulate(4)(k => deck.draw.getOrElse(new Card(52))) do //to the table
      tableCards += card

  def updateScores(): Unit =    // For updating scores after each round
    for player <- players do
      val index = player.playerNumber
      scores(index) = (player , scores(index)._2 + player.returnRoundScore)
      println(scores(index))
    //scores.foreach(k => scores.update(scores.indexOf(k), (k._1, k._2 + k._1.returnRoundScore)))


  def returnTopScore: (Player, Int) = scores.maxBy(_._2)

  def returnScore(player: Player): Int = scores.find(_._1 == player).getOrElse(((new Human(-1, "Invalid player")), -1))._2

  def saveGame(): Unit = ReaderWriter.writeSaveFile(this)

  def addCardToTable(card: Card): Unit = tableCards += card

  def removeCardFromTable(card: Card): Unit = tableCards -= card

  def setDealer(newDealer: Int): Unit = this.dealer = newDealer

  def setTurn(newTurn: Int): Unit  = this.turn = newTurn % players.size

  def setLastTaker(newTakerNumber: Int): Unit = this.lastToTake = newTakerNumber

  def setRoundOver(newRoundNumber: Int): Unit = this.roundNumber = newRoundNumber

  def setGameOver(newGameOver: Boolean): Unit = this.gameOver = newGameOver

  def addPlayerScores(player: Player, score: Int): Unit = scores.update(scores.indexOf(scores.find(_._1 == player).get), (player, score)) // Set correct scores for players when loading from a file

  def addPlayer(player: Player): Unit = players += player

  def returnRoundNumber: Int = roundNumber

  def returnGameOver: Boolean = gameOver
}



