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

  def addCardToPlayer(card: Card): Unit

  def addPoints(add: Int): Unit

  def returnPileSize: Int

  def returnSpadesSize: Int
}
