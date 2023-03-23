package Cassino

import scala.collection.mutable.Buffer

case class COM(val playerNumber: Int, val playerName: String, game: Game) extends Player {

  private var playerScore: Int          = 0

  val cards:               Buffer[Card] = Buffer()
  val pile:                Buffer[Card] = Buffer()

  def returnRoundScore: Int =
    /// Round score calculated here from this.cards
    var roundScore = playerScore
    playerScore = 0
    cards.foreach(k => if k.handValue == 14 || k.cardID == 39 then roundScore += 1 else if k.cardID == 21 then roundScore += 2) //For aces and 2 of Spades you get 1 point, for 10 of Diamonds you get 2 points
    roundScore

  def clearHand: Unit = cards.empty //Clears player's hand for a new round formerly .drop(cards.size)
  
  def addCardToPile(card: Card)   = pile += card

  def addCardToPlayer(card: Card) = cards += card

  def addPoints(add: Int): Unit =
    playerScore += add

  def returnPileSize: Int = pile.size

  def returnSpadesSize: Int = pile.count(_.suit == 3)

  override def toString: String =
    playerName + ", Player Number: " + playerNumber
}