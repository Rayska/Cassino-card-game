package Cassino

import Cassino.io.ReaderWriter
import com.sun.prism.impl.Disposer.Target

import scala.collection.mutable.Buffer


class Game(var players: Buffer[Player], var deck: Deck = new Deck){

val vNumber:      String                         = "0001"
private val scores:       Buffer[(Player, Int)]          = players.zip(Buffer.fill(players.size)(0))
private val tableCards:   Buffer[Card]                   = Buffer()
private var dealer:       Int                            = 0
private var turn:         Int                            = (dealer + 1) % players.size // Round starts from the player next to the dealer
private var lastToTake:   Int                            = -1                          // Player nummber of the last players who took cards from the table
private var roundNumber:  Int                            = 0
private var gameOver:     Boolean                        = false
private var playLog: Buffer[(Player, Card, Buffer[Card])] = Buffer[(Player, Card, Buffer[Card])]()

  def setupRound(): Unit = //Called when a new round starts
    this.deck = new Deck()
    tableCards.clear()
    players.foreach(k =>
      k.clearHand()
      k.clearPile()
      k.clearSweeps())
    this.dealCards()
    roundNumber += 1
    while !gameOver && !players.forall(_.cards.isEmpty) && (players(turn) match //Play consecutively all COMs' turns
    case COM(playerNumber, playerName, gameOption) => true
    case _ => false)
    do
      turnCheck()

  def endRound(): Unit =  //Return value states if game is over
    val multipleScoreReceiversAllowed = false  //Can be decided later whether everyone with most cards/spades gets the points.
    tableCards.foreach(players.find(_.playerNumber == lastToTake).get.addCardToPile(_))
    val mostCards = players.groupBy(_.returnPileSize)(players.groupBy(_.returnPileSize).keys.max)
    val mostSpades = players.groupBy(_.returnSpadesSize)(players.groupBy(_.returnSpadesSize).keys.max)
    if multipleScoreReceiversAllowed then
      mostCards.foreach(_.addPoints(1))  // Add points for most cards
      mostSpades.foreach(_.addPoints(2)) // Add points for most Spades
    else
      if mostCards.size == 1 then mostCards.head.addPoints(1) // Add points for most cards
      if mostSpades.size == 1 then mostSpades.head.addPoints(2) // Add points for most Spades
    this.updateScores()
    gameOver = scores.exists(_._2 >= 16)
    dealer = (dealer + 1) % players.size
    turn = (dealer + 1) % players.size
    lastToTake = -1
    if gameOver then
      tableCards.clear()

  def playTurn(usedForTaking: Buffer[Card], toBeTaken: Buffer[Card]) : Boolean = //This is called for every play by User and COM with card(s) from hand and card(s) from table
    var whosTurn = players(turn % players.size)
    if usedForTaking.nonEmpty then
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
      else if usedForTaking.head.cardID == 52 then
        throw InvalidCardException(s"Card used for taking has invalid cardID: ${usedForTaking.head.cardID}") //Only created in COM.placing()
      if players.forall(_.cards.isEmpty) then
        playLog += ((whosTurn, usedForTaking.head, toBeTaken))
        endRound()
        if !gameOver then
          setupRound()
        return true
      else
        playLog += ((whosTurn, usedForTaking.head, toBeTaken))
        return true
    false

  def turnCheck(): Boolean = //Check if it is a COM's turn. If it is, make them play
    players(turn) match
      case COM(number: Int, name: String, game: Option[Game]) =>
        players(turn).makePlay()
        true
      case _ =>
        false

  def checkMoveLegitimacyTake(usedForTaking: Buffer[Card], toBeTaken: Buffer[Card]): Boolean =
    val noDuplicates  = toBeTaken.map(_.cardID) == toBeTaken.map(_.cardID).distinct                                   // There are no duplicates in the cards we are trying to take (same card cannot be taken multiple times)
    val playerHasCard = checkPlayerHasCard(usedForTaking)                                                             // Player has every(/the) card in their hand they are trying to use
    val tableHasCard  = toBeTaken.forall(k => tableCards.exists(_.cardID == k.cardID))                                // Cards we are trying to take are on the Table
    val oneAndSame    = onlyOneDistinct(usedForTaking)                                                                // Same card is used for every "taking" move
    if !(noDuplicates && playerHasCard && tableHasCard && oneAndSame) then
      return false
    SumCheck(usedForTaking.head, toBeTaken)

  def SumCheck(usedForTaking: Card, toBeTaken: Buffer[Card]): Boolean = //Check if tableValues.sum are divisible by handValue
    val handValue = usedForTaking.handValue
    val tableValues = toBeTaken.map(_.tableValue)
    if !(tableValues.sum % handValue == 0) then
      return false
    val numOfPartitions = tableValues.sum / handValue
    partitionCheck(tableValues, numOfPartitions)

  def partitionCheck(tableValues: Buffer[Int], numOfPartitions: Int): Boolean = //Check if tableValues can be partitioned into numOfPartitions so that each partition has an equal sum
    val sum = tableValues.sum
    if numOfPartitions == 0 || sum % numOfPartitions != 0 then  // If the sum is not divisible by numOfPartitions, then it's not possible to partition the array into numOfPartitions sub-buffers with equal sums
      return false
    val targetSum = sum / numOfPartitions
    val bufferSum = Array.fill(numOfPartitions)(0)
    def recursive(i: Int): Boolean = // A recursive helper function to partition the Buffer
      var ret = false
      if i == tableValues.length then// If reached the end of the Buffer, check if each sub-buffer has a sum equal to the target sum
        return bufferSum.forall(_ == targetSum)
      for j <- 0 until numOfPartitions do
        if targetSum >= bufferSum(j) + tableValues(i) then //If current element can be added to bufferSum(j) without the sum going over targetSum then add it
          bufferSum(j) += tableValues(i)
          if recursive(i + 1) then // Recursively try to add the next element to the sub-buffers
            ret = true
          bufferSum(j) -= tableValues(i)  // If the recursive call didn't succeed, remove the element from the sub-buffer and try adding it to the next sub-buffer
      ret                      // couldn't add current element to any sub-buffer so return false
    recursive(0) // Call the helper function with the first element of the Buffer

  def checkPlayerHasCard(move: Buffer[Card]): Boolean =
    var truth = true
    var index = 0
    while truth && index < move.size do
      if !players(turn % players.size).cards.exists(_.cardID == move(index).cardID) then
        truth = false
      index += 1
    truth

  def onlyOneDistinct(usedForTaking: Buffer[Card]) = usedForTaking.forall(usedForTaking.head.cardID == _.cardID) // no multiple cards from hand

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

  def returnScores = scores

  def returnTableCards = tableCards

  def returnDealer = dealer

  def returnTurn = turn

  def returnLastToTake = lastToTake

  def returnPlayLog = playLog

}



