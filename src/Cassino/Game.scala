package Cassino

import Cassino.io.ReaderWriter

import scala.collection.mutable.Buffer
import scala.swing.*

class Game(val players: Vector[Player], val deck: Deck = new Deck){

  val vNumber:      String                = "0001"
  val scores:       Buffer[(Player, Int)] = players.zip(Vector.fill(players.size)(0)).toBuffer
  val tableCards:   Buffer[Card]          = Buffer()
  var dealer:       Int                   = 0
  var turn:         Int                   = (dealer + 1) % players.size // Round starts from the player next to the dealer
  var endCondition: Boolean               = false

  def playGame: Unit =
    while !endCondition do
      this.playRound
      this.updateScores
      dealer += 1
      turn = (dealer + 1) % players.size
      endCondition = !scores.forall(_._2 < 16)
      //endCondition = true//TEMP
    println(s"Game has ended with ${scores.maxBy(_._2)._1} as the Winner!")

  def playRound: Unit =
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

  def playTurn: (Buffer[(Card, Buffer[Card])], Buffer[Card]) =
    val whosTurn = players(turn % players.size)                                                                         // Which player's turn it is (indexes so starts from 0)
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
    (Buffer((new Card(52), Buffer(new Card(52), new Card(52)))), Buffer(new Card(52)))  //

  def checkMoveLegitimacyTake(take: Buffer[(Card, Buffer[Card])]): Boolean =
    //For each taking move the player has to determine which card is used to pick which cards
    val toBeTaken     = take.collect(_._2).flatten                                                                    // Cards which the player is trying to take off the table
    val noDuplicates  = toBeTaken == toBeTaken.distinct                                                               // There are no duplicates in the cards we are trying to take (same card cannot be taken multiple times)
    val playerHasCard = checkPlayerHasCard(take.collect(_._1))                                                        // Player has every(/the) card in their hand they are trying to use
    val tableHasCard  = take.forall(k => k._2.forall(l => tableCards.exists(_.cardID == l.cardID)))                   // Cards we are trying to take are on the Table
    val oneAndSame    = take.forall(take.head._1.cardID == _._1.cardID)                                               // Same card is used for every "taking" move
    val isEqual       = take.forall(k => (k._1.handValue == k._2.foldLeft(0)((l: Int, m: Card) => l + m.tableValue))) // handValue of card in use is equal to table cards' tableValue
    noDuplicates && playerHasCard && tableHasCard && oneAndSame && isEqual

  def checkPlayerHasCard(place: Buffer[Card]): Boolean =
    var truth = true
    var index = 0
    while truth && index < place.size do
      if !players(turn % players.size).cards.exists(_.cardID == place(index).cardID) then
        truth = false
      index += 1
    truth

  def dealCards: Unit =
    for player <- players do //to the players
      val hand = Buffer.tabulate(4)(k => deck.draw.getOrElse(new Card(52)))   // Card(52).toString will be 2 of Unknown suit
      for card <- hand do
        player.addCardToPlayer(card)
    for card <- Buffer.tabulate(4)(k => deck.draw.getOrElse(new Card(52))) do //to the table
      tableCards += card

  def updateScores: Unit =
    for score <- scores do
      scores.update(scores.indexOf(score), (score._1, score._2 + score._1.returnRoundScore))

  def returnTopScore: (Player, Int) = scores.maxBy(_._2)

  def returnScore(player: Player): Int = scores.find(_._1 == player).getOrElse(((new Human(-1, "Invalid player")), -1))._2

  def saveGame = ReaderWriter.writeSaveFile(this)

  def addCardToTable(card: Card): Unit = tableCards += card

  def setDealer(newDealer: Int): Unit = this.dealer = newDealer

  def setTurn(newTurn: Int): Unit  = this.turn = newTurn % players.size

}