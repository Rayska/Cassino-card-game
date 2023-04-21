package Cassino

import scala.collection.mutable.Buffer

abstract class Player {

  val cards:        Buffer[Card]
  val pile:         Buffer[Card]
  val playerNumber: Int
  val playerName:   String

  def returnRoundScore: Int
  
  def clearHand: Unit

  def addCardToPile(card: Card):   Unit

  def addCardToPlayer(cardOption: Option[Card]): Unit

  def addPoints(add: Int): Unit

  def returnPileSize: Int

  def returnSpadesSize: Int

  def changeGame(game: Game): Unit

  def returnGame: Game
}
