package Cassino

import scala.collection.mutable.Buffer

case class Human(val playerNumber: Int, val playerName: String) extends Player {

  private var gameOption: Option[Game] = None

  private var additionalPoints: Int          = 0 //Points for most cards, most spades

  val cards:               Buffer[Card] = Buffer()
  val pile:                Buffer[Card] = Buffer()
  var sweeps:              Int          = 0

  def returnRoundScore: Int = // Round score calculated here from this.cards
    var roundScore = additionalPoints + sweeps
    additionalPoints = 0
    pile.foreach(k =>                             //For aces and 2 of Spades you get 1 point, for 10 of Diamonds you get 2 points
      if k.handValue == 14 || k.cardID == 39 then 
        roundScore += 1 
      else if k.cardID == 21 then 
        roundScore += 2) 
    roundScore

  def clearHand(): Unit = cards.clear //Clears player's hand for a new round formerly .drop(cards.size)

  def clearPile(): Unit = pile.clear

  def clearSweeps(): Unit = sweeps = 0

  def addCardToPile(card: Card)   = pile += card

  def addCardToPlayer(cardOption: Option[Card]) = if cardOption.nonEmpty then cards += cardOption.get
  
  def removeCardFromPlayer(card: Card): Unit = cards -= card

  def addPoints(add: Int): Unit = additionalPoints += add
    
  def addSweep(): Unit = sweeps += 1

  def returnPileSize: Int = pile.size

  def returnSpadesSize: Int = pile.count(_.suit == 3)

  def returnAcesSize: Int = pile.count(_.tableValue == 1)
  
  def returnSweeps: Int = sweeps

  def changeGame(newgame: Game): Unit = gameOption = Some(newgame)

  def returnGame: Game = gameOption.get
  
  def makePlay(): Unit = {}

  def returnCards: Buffer[Card] = cards

  def returnPile: Buffer[Card] = pile

  override def toString: String =
    playerName + ", Human" + ", Player Number: " + playerNumber
}
