package Cassino

import scala.collection.mutable.Buffer

case class COM(val playerNumber: Int, val playerName: String, var game: Option[Game] = None) extends Player {

  private var playerScore: Int          = 0

  val cards:               Buffer[Card] = Buffer()
  val pile:                Buffer[Card] = Buffer()
  var sweeps:              Int          = 0

  def returnRoundScore: Int =
    /// Round score calculated here from this.cards
    var roundScore = playerScore
    playerScore = 0
    pile.foreach(k => if k.handValue == 14 || k.cardID == 39 then roundScore += 1 else if k.cardID == 21 then roundScore += 2) //For aces and 2 of Spades you get 1 point, for 10 of Diamonds you get 2 points
    roundScore += sweeps
    roundScore

  def clearHand(): Unit = cards.clear //Clears player's hand for a new round formerly .drop(cards.size)

  def clearPile(): Unit = pile.clear

  def clearSweeps(): Unit = sweeps = 0

  def addCardToPile(card: Card)   = pile += card

  def addCardToPlayer(cardOption: Option[Card]) = if cardOption.nonEmpty then cards += cardOption.get

  def removeCardFromPlayer(card: Card): Unit = cards -= card

  def addPoints(add: Int): Unit =
    playerScore += add

  def addSweep(): Unit = sweeps += 1

  def returnPileSize: Int = pile.size

  def returnSpadesSize: Int = pile.count(_.suit == 3)
  
  def returnAcesSize: Int = pile.count(_.tableValue == 1)
  
  def returnSweeps: Int = sweeps

  def changeGame(newgame: Game): Unit = game = Some(newgame)

  def returnGame: Game = game.get

  override def toString: String =
    playerName + ", COM" + ", Player Number: " + playerNumber
}
