package Cassino

import scala.collection.mutable.Buffer

class Human(val playerNumber: Int, val playerName: String) extends Player {

  private var playerScore: Int          = 0

  val cards:               Buffer[Card] = Buffer()
  val pile:                Buffer[Card] = Buffer()

  def returnRoundScore: Int =
    /// Round score calculated here from this.cards
    var roundScore = playerScore
    playerScore = 0
    cards.foreach(k => if k.handValue == 14 || k.cardID == 39 then roundScore += 1 else if k.cardID == 21 then roundScore += 2) //For aces and 2 of Spades you get 1 point, for 10 of Diamonds you get 2 points
    roundScore

  def addCardToPile(card: Card)   = pile += card

  def addCardToPlayer(card: Card) = cards += card

  def addExtraScore(possibleSweeps: Int, possiblyMostCards: Boolean, possiblyMostSpades: Boolean) =
    playerScore += possibleSweeps
    if possiblyMostCards  then playerScore += 1
    if possiblyMostSpades then playerScore += 2
}
