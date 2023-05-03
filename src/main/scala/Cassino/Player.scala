package Cassino

import scala.collection.mutable.Buffer

abstract class Player {

  val playerNumber: Int
  val playerName:   String

  val cards:        Buffer[Card]
  val pile:         Buffer[Card]
  var sweeps:       Int

  def makePlay(): Unit

  def returnRoundScore: Int
  
  def clearHand(): Unit
  
  def clearPile(): Unit

  def clearSweeps(): Unit

  def addCardToPile(card: Card):   Unit

  def addCardToPlayer(cardOption: Option[Card]): Unit
  
  def removeCardFromPlayer(card: Card): Unit

  def addPoints(add: Int): Unit

  def addSweep(): Unit
  
  def returnPileSize: Int

  def returnSpadesSize: Int

  def returnAcesSize: Int

  def returnSweeps: Int

  def changeGame(game: Game): Unit

  def returnGame: Game
  
  def returnCards: Buffer[Card]
  
  def returnPile: Buffer[Card]
}
