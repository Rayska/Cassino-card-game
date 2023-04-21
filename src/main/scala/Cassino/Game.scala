package Cassino

import Cassino.io.ReaderWriter
import com.sun.prism.impl.Disposer.Target

import scala.collection.mutable.Buffer


class Game(var players: Buffer[Player], var deck: Deck = new Deck){

  val vNumber:      String                = "0001"
  val scores:       Buffer[(Player, Int)] = players.zip(Buffer.fill(players.size)(0))
  val tableCards:   Buffer[Card]          = Buffer()
  var dealer:       Int                   = 0
  var turn:         Int                   = (dealer + 1) % players.size // Round starts from the player next to the dealer
  var gameOver: Boolean                   = false

  def playGame: Unit =
    while !gameOver do
      //this.playRound
      this.updateScores
      dealer += 1
      turn = (dealer + 1) % players.size
      gameOver = !scores.forall(_._2 < 16)
      //endCondition = true//TEMP
    println(s"Game has ended with ${scores.maxBy(_._2)._1} as the Winner!")

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
    players.foreach(_.clearHand)
    this.dealCards

  def endRound(): Unit =
    val mostCards  = players.filter(k => k.returnPileSize == players.maxBy(l => l.returnPileSize).returnPileSize)       // Players with largest piles
    val mostSpades = players.filter(k => k.returnSpadesSize == players.maxBy(l => l.returnSpadesSize).returnSpadesSize) // Players with most Spades
    mostCards.foreach(_.addPoints(1))                                                                                   // Add points for most cards
    mostSpades.foreach(_.addPoints(2))                                                                                  // Add points for most Spades
    this.deck.clear()
    this.updateScores                                                                                                   // Should also clear each players' roundScore since this leads to calling player.returnRoundScore
    gameOver = scores.exists(_._2 >= 16)

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

  def playTurn(UsedForTaking: Buffer[Card], toBeTaken: Buffer[Card]) : Boolean =
    var whosTurn = players(turn % players.size)
    if UsedForTaking.nonEmpty && UsedForTaking.forall(UsedForTaking.head == _) then
      if toBeTaken.nonEmpty && checkPlayerHasCard(UsedForTaking) then
        false
    return false

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
    val noDuplicates  = toBeTaken == toBeTaken.distinct                                                               // There are no duplicates in the cards we are trying to take (same card cannot be taken multiple times)
    val playerHasCard = checkPlayerHasCard(usedForTaking)                                                             // Player has every(/the) card in their hand they are trying to use
    val tableHasCard  = toBeTaken.forall(k => tableCards.exists(_.cardID == k.cardID))                                // Cards we are trying to take are on the Table
    val oneAndSame    = onlyOneDistinct(usedForTaking)                                                                // Same card is used for every "taking" move
    if noDuplicates && playerHasCard && tableHasCard && oneAndSame then
      SumCheck(usedForTaking.head, toBeTaken)
    else
      false
    //recursive search here

  def SumCheck(usedForTaking: Card, toBeTaken: Buffer[Card]): Boolean =
    val targetOriginal = usedForTaking.handValue
    val buffOriginal = toBeTaken.map(_.tableValue).filterNot(_ == targetOriginal).sorted.reverse
    val removed = Buffer[Int]()
    def recursiveSumCheck(target: Int, buff: Buffer[Int]): Boolean =
      var cursor = buff.head
      var collector = cursor
      var temp = Buffer[Int](collector)
      var i = buff.size - 1
      var found = false
      while i > 0 && buff.nonEmpty do
        if collector + buff(i) <= target then
          collector += buff(i)
          temp += buff(i)
          if collector == target then
            temp.foreach(k => {
              //buffOriginal.remove(buffOriginal.indexOf(k))
              removed += k
              //buff.remove(buff.indexOf(k))
              found = true
            })
            temp.clear()
            if buff.nonEmpty then
              return true
        else if i == 1 && recursiveSumCheck(target - cursor, buff.tail) then
          removed += cursor
          //buffOriginal.remove(buffOriginal.indexOf(cursor))
          //buff.remove(buff.indexOf(cursor))
          if !(removed.sorted.reverse == buffOriginal) then
            recursiveSumCheck(target, buffOriginal)
        i -= 1
      return found
    recursiveSumCheck(targetOriginal, buffOriginal)






  def checkPlayerHasCard(move: Buffer[Card]): Boolean =
    var truth = true
    var index = 0
    while truth && index < move.size do
      if !players(turn % players.size).cards.exists(_.cardID == move(index).cardID) then
        truth = false
      index += 1
    truth

  def onlyOneDistinct(usedForTaking: Buffer[Card]) = usedForTaking.forall(usedForTaking.head.cardID == _.cardID)

  def dealCards: Unit =
    for player <- players do //to the players
      val hand = Buffer.tabulate(4)(k => deck.draw.getOrElse(new Card(52)))   // Card(52).toString will be 2 of Unknown suit
      for card <- hand do
        player.addCardToPlayer(Some(card))
    for card <- Buffer.tabulate(4)(k => deck.draw.getOrElse(new Card(52))) do //to the table
      tableCards += card

  def updateScores: Unit =    // For updating scores after each round
    //for score <- scores do
     scores.foreach(k => scores.update(scores.indexOf(k), (k._1, k._2 + k._1.returnRoundScore)))

  def returnTopScore: (Player, Int) = scores.maxBy(_._2)

  def returnScore(player: Player): Int = scores.find(_._1 == player).getOrElse(((new Human(-1, "Invalid player")), -1))._2

  def saveGame = ReaderWriter.writeSaveFile(this)

  def addCardToTable(card: Card): Unit = tableCards += card

  def setDealer(newDealer: Int): Unit = this.dealer = newDealer

  def setTurn(newTurn: Int): Unit  = this.turn = newTurn % players.size

  def addPlayerScores(player: Player, score: Int): Unit = scores.update(scores.indexOf(scores.find(_._1 == player).get), (player, score)) // Set correct scores for players when loading from a file

  def addPlayer(player: Player): Unit = players += player

}
